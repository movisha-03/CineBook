
import os
from pathlib import Path

import pandas as pd
import matplotlib.pyplot as plt
import mysql.connector
from mysql.connector import Error


# Output folder for analytics charts
OUT = Path(__file__).resolve().parent / "output"
OUT.mkdir(exist_ok=True)


def main():

    print("ANALYTICS STARTED")

    conn = None

    try:
        # Step 1: Connect to MySQL database
        conn = mysql.connector.connect(
            host=os.getenv("DB_HOST", "localhost"),
            port=int(os.getenv("DB_PORT", "3306")),
            database=os.getenv("DB_NAME", "cinebook_db"),
            user=os.getenv("DB_USERNAME", "root"),
            password=os.getenv("DB_PASSWORD", ""),
            use_pure=True
        )

        if conn.is_connected():
            print("DATABASE CONNECTED SUCCESSFULLY")

        # Step 2: SQL query to collect movie sales data
        query = """
        WITH sales AS (
            SELECT
                b.show_id,
                COUNT(bs.seat_id) AS booked_seats,
                SUM(bs.ticket_price) AS gross_sales
            FROM bookings b
            JOIN booking_seats bs
                ON bs.booking_id = b.booking_id
            WHERE b.booking_status = 'CONFIRMED'
            GROUP BY b.show_id
        ),

        refund_totals AS (
            SELECT
                b.show_id,
                SUM(p.amount) AS refunds
            FROM bookings b
            JOIN payments p
                ON p.booking_id = b.booking_id
            WHERE p.payment_status = 'REFUNDED'
            GROUP BY b.show_id
        )

        SELECT
            m.title AS movie_title,
            s.show_date,
            sc.capacity,
            COALESCE(sa.booked_seats, 0) AS booked_seats,
            COALESCE(sa.gross_sales, 0) AS gross_sales,
            COALESCE(rt.refunds, 0) AS refunds

        FROM shows s

        JOIN movies m
            ON m.movie_id = s.movie_id

        JOIN screens sc
            ON sc.screen_id = s.screen_id

        LEFT JOIN sales sa
            ON sa.show_id = s.show_id

        LEFT JOIN refund_totals rt
            ON rt.show_id = s.show_id

        ORDER BY s.show_date
        """

        # Step 3: Load SQL data into Pandas DataFrame
        df = pd.read_sql(query, conn)

        print("\nDATABASE DATA LOADED SUCCESSFULLY")

        if df.empty:
            print("No show data found. Add movies and shows first.")
            return

        # Step 4: Convert numerical columns
        numeric_columns = [
            "capacity",
            "booked_seats",
            "gross_sales",
            "refunds"
        ]

        for col in numeric_columns:
            df[col] = pd.to_numeric(
                df[col], errors="coerce"
            ).fillna(0)

        # Step 5: Calculate theatre occupancy percentage
        valid_capacity = df["capacity"].where(df["capacity"] > 0)

        df["occupancy_pct"] = (
            df["booked_seats"] / valid_capacity * 100
        ).fillna(0)

        # Step 6: Calculate net revenue
        df["net_revenue"] = (
            df["gross_sales"] - df["refunds"]
        )

        # Step 7: Generate movie-wise summary
        summary = df.groupby(
            "movie_title", as_index=False
        ).agg(
            tickets_sold=("booked_seats", "sum"),
            gross_sales=("gross_sales", "sum"),
            refunds=("refunds", "sum"),
            net_revenue=("net_revenue", "sum"),
            average_occupancy=("occupancy_pct", "mean")
        )

        # Step 8: Display analytics report
        print("\n========== CINEBOOK ANALYTICS REPORT ==========")

        print(
            summary.sort_values(
                "net_revenue", ascending=False
            ).to_string(index=False)
        )

        # Step 9: Net revenue chart
        summary.plot(
            x="movie_title",
            y="net_revenue",
            kind="bar",
            legend=False,
            title="Net Revenue by Movie"
        )

        plt.ylabel("Revenue (INR)")
        plt.tight_layout()
        plt.savefig(OUT / "revenue_by_movie.png")
        plt.close()

        # Step 10: Theatre occupancy trend chart
        df["show_date"] = pd.to_datetime(df["show_date"])

        df.groupby("show_date")["occupancy_pct"].mean().plot(
            kind="line",
            marker="o",
            title="Average Theatre Occupancy"
        )

        plt.ylabel("Occupancy (%)")
        plt.tight_layout()
        plt.savefig(OUT / "occupancy_trend.png")
        plt.close()

        # Step 11: Movie popularity chart
        summary.sort_values(
            "tickets_sold"
        ).plot(
            x="movie_title",
            y="tickets_sold",
            kind="barh",
            legend=False,
            title="Movie Popularity"
        )

        plt.xlabel("Tickets Sold")
        plt.tight_layout()
        plt.savefig(OUT / "movie_popularity.png")
        plt.close()

        # Step 12: Final output
        print("\nANALYTICS COMPLETED SUCCESSFULLY!")
        print("Charts saved in:", OUT)

    except Error as e:
        print("\nDATABASE ERROR:", e)

    except Exception as e:
        print("\nERROR:", e)

    finally:
        if conn is not None and conn.is_connected():
            conn.close()
            print("\nDATABASE CONNECTION CLOSED")


if __name__ == "__main__":
    main()
