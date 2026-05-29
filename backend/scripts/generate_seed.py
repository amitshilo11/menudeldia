#!/usr/bin/env python3
"""
Read restaurants_db_ready.csv and write src/main/resources/seed.json.
Run from the backend/ directory:
    python3 scripts/generate_seed.py
"""

import csv
import json
import sys
from pathlib import Path

CSV = Path(__file__).parent.parent / "data" / "restaurants_db_ready.csv"
OUT = Path(__file__).parent.parent / "src/main/resources/seed.json"

# ---------------------------------------------------------------------------
# Cuisine: CSV value → Cuisine enum name
# ---------------------------------------------------------------------------

CUISINE_MAP = {
    "Asian":                   "ASIAN",
    "Asian, Mediterranean":    "ASIAN",
    "Chinese":                 "ASIAN",
    "Vietnamese":              "ASIAN",
    "Korean":                  "ASIAN",
    "Korean, Korean BBQ":      "ASIAN",
    "Japanese":                "JAPANESE",
    "Japanese, Sushi":         "JAPANESE",
    "Sushi":                   "JAPANESE",
    "Ramen":                   "JAPANESE",
    "Italian":                 "ITALIAN",
    "Mexican":                 "MEXICAN",
    "Indian":                  "INDIAN",
    "Nepalese":                "INDIAN",
    "Spanish":                 "SPANISH",
    "Spanish, Gourmet":        "SPANISH",
    "Spanish, Grill":          "SPANISH",
    "Spanish, Meat":           "SPANISH",
    "Spanish, Tapas":          "SPANISH",
    "Catalan":                 "SPANISH",
    "Catalan, Gastronomy":     "SPANISH",
    "Catalan, Gluten-Free":    "SPANISH",
    "Catalan, Meat":           "SPANISH",
    "Catalan, Tapas":          "SPANISH",
    "Mediterranean":           "MEDITERRANEAN",
    "Mediterranean, Bistro":   "MEDITERRANEAN",
    "Mediterranean, Fusion":   "MEDITERRANEAN",
    "Mediterranean, Grill":    "MEDITERRANEAN",
    "Mediterranean, Healthy":  "MEDITERRANEAN",
    "Mediterranean, Spanish":  "MEDITERRANEAN",
    "Vegetarian":              "MEDITERRANEAN",
    "Oyster Restaurant":       "MEDITERRANEAN",
    "French-Catalan, Bistro":  "MEDITERRANEAN",
    "Argentinian":             "OTHER",
    "Brunch":                  "OTHER",
    "Burgers":                 "OTHER",
    "Colombian":               "OTHER",
    "Ecuadorian":              "OTHER",
    "Peruvian":                "OTHER",
    "Venezuelan":              "OTHER",
    "Gluten-Free":             "OTHER",
}

CUISINE_EMOJI = {
    "SPANISH":        "🥘",
    "MEDITERRANEAN":  "🫒",
    "ASIAN":          "🥢",
    "JAPANESE":       "🍣",
    "ITALIAN":        "🍝",
    "MEXICAN":        "🌮",
    "MIDDLE_EASTERN": "🥙",
    "INDIAN":         "🍛",
    "OTHER":          "🍽️",
}

# ---------------------------------------------------------------------------
# Menu component translation: English → Spanish
# ---------------------------------------------------------------------------

COMPONENT_ES = {
    "Starter":           "Primero",
    "Main":              "Principal",
    "Dessert":           "Postre",
    "Drink":             "Bebida",
    "Water":             "Agua",
    "Coffee":            "Café",
    "Appetizer":         "Entrante",
    "Sushi":             "Sushi",
    "Dim Sum":           "Dim sum",
    "Pita":              "Pan de pita",
    "Ramen":             "Ramen",
    "Burger":            "Hamburguesa",
    "Fries":             "Patatas fritas",
    "Main (Ramen)":      "Principal (Ramen)",
    "Bowl of choice":    "Bol a elegir",
    "3 dishes of choice":  "3 platos a elegir",
    "4 plates of choice":  "4 platos a elegir",
    "4 dishes of choice":  "4 platos a elegir",
    "5 dishes of choice":  "5 platos a elegir",
    "Hot Pot (6 plates)":  "Hot Pot (6 platos)",
    "Free buffet – no drink, no dessert": "Bufé libre (sin bebida ni postre)",
}

def translate_includes(menu_details: str):
    """Split 'A + B + C' into EN list and translated ES list."""
    if not menu_details.strip():
        return [], []
    parts = [p.strip() for p in menu_details.split("+")]
    en_list = parts
    es_list = []
    for part in parts:
        es = COMPONENT_ES.get(part)
        if es:
            es_list.append(es)
        else:
            es_list.append(part)  # fallback: keep original
            print(f"  ⚠ no ES translation for: {repr(part)}", file=sys.stderr)
    return es_list, en_list

# ---------------------------------------------------------------------------
# Schedule: structured CSV columns → weekdayHours dict
# ---------------------------------------------------------------------------

DAY_KEY = {"Mon": "mon", "Tue": "tue", "Wed": "wed", "Thu": "thu", "Fri": "fri"}

ALL_WEEKDAYS = ["mon", "tue", "wed", "thu", "fri"]

def build_weekday_range(days_from: str, days_to: str) -> list:
    start = ALL_WEEKDAYS.index(DAY_KEY.get(days_from, "mon"))
    end   = ALL_WEEKDAYS.index(DAY_KEY.get(days_to,   "fri"))
    return ALL_WEEKDAYS[start:end + 1]

def build_weekday_hours(days_from: str, days_to: str, excluded_day: str,
                         open_time: str, close_time: str) -> dict:
    days = build_weekday_range(days_from, days_to)
    if excluded_day:
        excl_key = DAY_KEY.get(excluded_day)
        if excl_key:
            days = [d for d in days if d != excl_key]
    time_range = f"{open_time}-{close_time}"
    return {d: time_range for d in days}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

print(f"Reading {CSV} ...")
records = []

with open(str(CSV), newline="", encoding="utf-8") as f:
    for row in csv.DictReader(f):
        name = row["name"].strip()
        if not name:
            continue

        cuisine_csv = row["cuisine_type"].strip()
        cuisine     = CUISINE_MAP.get(cuisine_csv, "OTHER")
        emoji       = CUISINE_EMOJI[cuisine]

        price_str = row["price_normal"].strip().replace(",", ".")
        try:
            menu_price = float(price_str) if price_str else None
        except ValueError:
            menu_price = None

        es_list, en_list = translate_includes(row["menu_details"])

        weekday_hours = build_weekday_hours(
            row["days_from"], row["days_to"], row["excluded_day"],
            row["open_time"], row["close_time"],
        )

        place_id = row["google_place_id"].strip() or None
        website  = row["website"].strip() or None
        phone    = row["phone"].strip() or None

        vegetarian_options = row.get("Vegeterian options", "").strip().lower() == "yes"
        gluten_free_options = row.get("Gluten free options", "").strip().lower() == "yes"

        record = {
            "name":               name,
            "googlePlaceId":      place_id,
            "phone":              phone,
            "website":            website,
            "menuPrice":          menu_price,
            "cuisineType":        cuisine,
            "cuisineEmoji":       emoji,
            "priceIncludesEs":    es_list,
            "priceIncludesEn":    en_list,
            "weekdayHours":       weekday_hours,
            "vegetarianOptions":  vegetarian_options,
            "glutenFreeOptions":  gluten_free_options,
        }
        records.append(record)
        print(f"  [{len(records):3d}] {name:42s}  {cuisine:15s}  {menu_price}€")

print(f"\nWriting {len(records)} records → {OUT}")
with open(str(OUT), "w", encoding="utf-8") as f:
    json.dump(records, f, ensure_ascii=False, indent=2)

print("Done.")
