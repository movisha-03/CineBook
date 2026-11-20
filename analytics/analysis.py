
import os
from pathlib import Path

import pandas as pd
import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
import mysql.connector
from mysql.connector import Error


# ==================================================
# 1. CINEBOOK ANALYTICS START
# ==================================================

print("======================================", flush=True)
print("       CINEBOOK ANALYTICS SYSTEM", flush=True)
print("======================================", flush=True)


# ==================================================
# 2. OUTPUT DIRECTORY
# ==================================================

PROJECT_DIR = Path(__file__).resolve().parent.parent

OUTPUT_DIR = (
    PROJECT_DIR
    / "backend"
    / "src"
    / "main"
    / "resources"
    / "static"
    / "analytics"
)

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

print("Output directory:", OUTPUT_DIR, flush=True)


# ==================================================
# 3. MYSQL DATABASE CONNECTION
# ==================================================

connection = None

try:

    print("\nConnecting to MySQL database...", flush=True)

    connection = mysql.connector.connect(
        host="127.0.0.1",
        port=3306,
        database="cinebook_db",
        user="root",
        password=os.getenv("DB_PASSWORD", ""),
        connection_timeout=5,
        use_pure=True
    )

    if connection.is_connected():
        print("DATABASE CONNECTED SUCCESSFULLY!", flush=True)


    # ==================================================
    # 4. FETCH SHOW AND BOOKING DATA
    # ==================================================

    print("\nFetching booking data...", flush=True)

    query = """
        SELECT
            s.show_id,
            s.show_date,
            s.show_time,
            s.ticket_price,
            m.title AS movie_title,
            sc.capacity,
            COUNT(bs.seat_id) AS booked_seats,
            COALESCE(SUM(bs.ticket_price), 0) AS gross_sales

        FROM shows s

        INNER JOIN movies m
            ON s.movie_id = m.movie_id

        INNER JOIN screens sc
            ON s.screen_id = sc.screen_id

        LEFT JOIN (
            SELECT
                b.show_id,
                bs.seat_id,
                bs.ticket_price

            FROM bookings b

            INNER JOIN booking_seats bs
                ON b.booking_id = bs.booking_id

            WHERE b.booking_status = 'CONFIRMED'

        ) bs
            ON s.show_id = bs.show_id

        GROUP BY
            s.show_id,
            s.show_date,
            s.show_time,
            s.ticket_price,
            m.title,
            sc.capacity

        ORDER BY
            s.show_date,
            s.show_time
    """

    df = pd.read_sql(query, connection)

    print("BOOKING DATA FETCHED SUCCESSFULLY!", flush=True)
    print("Total shows:", len(df), flush=True)


    # ==================================================
    # 5. DATA PROCESSING
    # ==================================================

    if df.empty:

        print("No show data available.", flush=True)

    else:

        df["booked_seats"] = df["booked_seats"].fillna(0)

        df["gross_sales"] = df["gross_sales"].fillna(0)

        df["capacity"] = df["capacity"].fillna(0)

        df["occupancy"] = (
            df["booked_seats"]
            / df["capacity"].replace(0, pd.NA)
        ) * 100

        df["occupancy"] = df["occupancy"].fillna(0)

        df["show_date"] = pd.to_datetime(df["show_date"])

        print("DATA PROCESSING COMPLETED!", flush=True)


        # ==================================================
        # 6. MOVIE-WISE REVENUE ANALYSIS
        # ==================================================

        movie_summary = (
            df.groupby("movie_title")
            .agg(
                total_booked_seats=("booked_seats", "sum"),
                total_revenue=("gross_sales", "sum"),
                total_shows=("show_id", "count")
            )
            .reset_index()
            .sort_values(
                by="total_revenue",
                ascending=False
            )
        )

        print("\nMOVIE-WISE SUMMARY")
        print(movie_summary)


        # ==================================================
        # 7. CHART 1 - MOVIE REVENUE
        # ==================================================

        plt.figure(figsize=(12, 6))

        plt.bar(
            movie_summary["movie_title"],
            movie_summary["total_revenue"],
            color="#e63946"
        )

        plt.title(
            "Movie-wise Revenue Analysis",
            fontsize=16,
            fontweight="bold"
        )

        plt.xlabel("Movie Name")
        plt.ylabel("Revenue (INR)")

        plt.xticks(
            rotation=35,
            ha="right"
        )

        plt.grid(
            axis="y",
            linestyle="--",
            alpha=0.3
        )

        plt.tight_layout()

        plt.savefig(
            OUTPUT_DIR / "movie_revenue.png",
            dpi=200,
            bbox_inches="tight"
        )

        plt.close()

        print("Chart 1: Movie Revenue Generated!", flush=True)


        # ==================================================
        # 8. CHART 2 - SHOW OCCUPANCY
        # ==================================================

        occupancy_df = df.sort_values(
            by=["show_date", "show_time"]
        ).copy()

        plt.figure(figsize=(16, 7))

        plt.bar(
            occupancy_df["show_id"].astype(str),
            occupancy_df["occupancy"],
            color="#e63946",
            width=0.7
        )

        plt.title(
            "Show-wise Seat Occupancy",
            fontsize=16,
            fontweight="bold"
        )

        plt.xlabel("Show ID", fontsize=12)
        plt.ylabel("Occupancy Percentage (%)", fontsize=12)

        plt.ylim(0, 100)

        # Display selected Show IDs to avoid overlapping labels
        tick_step = max(
            1,
            len(occupancy_df) // 15
        )

        tick_positions = list(
            range(0, len(occupancy_df), tick_step)
        )

        plt.xticks(
            tick_positions,
            occupancy_df["show_id"].astype(str).iloc[
                tick_positions
            ],
            rotation=45,
            ha="right"
        )

        plt.grid(
            axis="y",
            linestyle="--",
            alpha=0.3
        )

        plt.tight_layout()

        plt.savefig(
            OUTPUT_DIR / "show_occupancy.png",
            dpi=200,
            bbox_inches="tight"
        )

        plt.close()

        print("Chart 2: Show Occupancy Generated!", flush=True)


        # ==================================================
        # 9. CHART 3 - DAILY REVENUE
        # ==================================================

        daily_summary = (
            df.groupby("show_date")["gross_sales"]
            .sum()
            .reset_index()
            .sort_values("show_date")
        )

        plt.figure(figsize=(12, 6))

        plt.plot(
            daily_summary["show_date"],
            daily_summary["gross_sales"],
            marker="o",
            color="#e63946",
            linewidth=2
        )

        plt.title(
            "Daily Revenue Analysis",
            fontsize=16,
            fontweight="bold"
        )

        plt.xlabel("Show Date")
        plt.ylabel("Revenue (INR)")

        plt.xticks(rotation=35)

        plt.grid(
            linestyle="--",
            alpha=0.3
        )

        plt.tight_layout()

        plt.savefig(
            OUTPUT_DIR / "daily_revenue.png",
            dpi=200,
            bbox_inches="tight"
        )

        plt.close()

        print("Chart 3: Daily Revenue Generated!", flush=True)


        # ==================================================
        # 10. SAVE MOVIE SUMMARY CSV
        # ==================================================

        movie_summary.to_csv(
            OUTPUT_DIR / "movie_summary.csv",
            index=False
        )

        print("Movie Summary CSV Generated!", flush=True)


        # ==================================================
        # 11. SUCCESS MESSAGE
        # ==================================================

        print("\n======================================")
        print("  ANALYTICS COMPLETED SUCCESSFULLY!")
        print("======================================")

        print("\nGenerated Files:")

        print("1. movie_revenue.png")
        print("2. show_occupancy.png")
        print("3. daily_revenue.png")
        print("4. movie_summary.csv")

        print("\nSaved Location:")
        print(OUTPUT_DIR)


except Error as e:

    print("\nMYSQL DATABASE ERROR:", e, flush=True)


except Exception as e:

    print("\nANALYTICS ERROR:", e, flush=True)


finally:

    if connection is not None and connection.is_connected():

        connection.close()

        print("\nDATABASE CONNECTION CLOSED", flush=True)
