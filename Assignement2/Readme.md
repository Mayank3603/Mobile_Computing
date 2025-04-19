# ✈️ MCA Assignment 2 – Flight Tracker & Analyzer

**Author:** Arnav Agarwal  
**Roll No.:** 2021235

This project demonstrates the use of APIs, Jetpack components, Room database, background jobs, and map visualizations in an Android application. It is divided into two parts: a real-time flight tracker and a flight data analytics tool.

---

## 📦 Project Structure

### 🧩 Question 1 – Live Flight Tracker

#### 📡 `AviationStackApi.kt`
Defines a Retrofit interface to access the AviationStack API's `flights` endpoint using `access_key`, `flight_number`, and `flight_status`.

#### 🧬 `AviationStackResponse.kt`
Kotlin data classes that model the API response:
- `AviationStackResponse`: Root response object.
- `Pagination`: Metadata about results.
- `FlightData`: Includes nested classes for:
  - `Departure` / `Arrival` info
  - `Airline` / `Flight` identifiers
  - Optional `Live` tracking data (altitude, latitude, longitude, etc.)

#### 📱 `FlightDetailsActivity.kt`
- Displays live flight info, updating UI every 1 second and re-fetching API data every 60 seconds.
- Shows: flight number, status, live location, scheduled times, and remaining time.
- Uses Geocoder to convert airport names to coordinates.
- Includes a **"Show Map"** button to visualize the route.

#### 🗺️ `FlightMapActivity.kt`
- Displays the departure, arrival, and live location on an **interactive OpenStreetMap** using the `osmdroid` library.
- Custom map markers for each location with proper scaling and anchoring.

#### 🔧 `RetrofitClient.kt`
- Singleton Retrofit instance setup with base URL and Gson converter.

#### 🧱 Layouts
- `activity_main.xml`: UI with a flight number input field and a "Fetch Flight Details" button.
- `activity_flight_details.xml`: Shows flight data in grouped cards with a "Show Map" button.
- `activity_flight_map.xml`: Fullscreen interactive map using osmdroid.

---

### 📊 Question 2 – Flight Data Analyzer

#### 🗃️ `Flight.kt`
Defines a Room `@Entity` representing a flight with fields like flight number, date, origin, destination, scheduled/actual times.

#### 🧪 `FlightDao.kt`
DAO interface with:
- Insert (with conflict handling)
- Delete
- Query by route

#### 🏛️ `AppDatabase.kt`
Room database configuration including `FlightDao`.

#### 🧰 `DatabaseProvider.kt`
Singleton helper to ensure a single instance of the Room database is used across the app.

#### 🔄 `Converters.kt`
Converts `LocalDateTime` ↔ `String` for Room compatibility.

#### ⚙️ `FetchFlightsWorker.kt`
- Background worker using `WorkManager` to fetch and store flight data periodically.
- Parses AviationStack JSON response into `Flight` entities.

---

### 🧾 UI Components

#### 📍 `activity_main.xml`
- Two dropdowns to choose origin & destination routes
- TextViews for:
  - Longest flight
  - Shortest flight
  - Overall average
  - Average per flight number
- A `RecyclerView` to list all flight records

#### 🧾 `item_flight.xml`
- Layout for each flight item in the list showing:
  - Route
  - Scheduled vs actual times
  - Duration & delay

---

### 🧑‍💻 `FlightAdapter.kt`
- Binds `Flight` data to views
- Parses date strings into readable formats
- Calculates & formats durations and delays
- Dynamically updates flight list via `updateFlights()`

---

### 🔁 `MainActivity.kt` (Question 2)
- Triggers periodic fetch via `WorkManager`
- Computes statistics:
  - Longest & shortest durations
  - Overall average flight time
  - Per-flight-number average durations
- Updates UI accordingly

---

## 🧠 Technologies Used

- Kotlin Coroutines
- Retrofit
- Room DB
- WorkManager
- osmdroid (for maps)
- Jetpack Architecture Components

---

## 📌 How to Run

1. Clone the project
2. Add your AviationStack API key in `FlightDetailsActivity.kt`
3. Make sure your emulator/device has internet access
4. Run the app and input a flight number like `AI302` or `EK202`

---

## 📍 Sample Inputs

- `AI302` – Air India
- `EK202` – Emirates
- `6E203` – IndiGo
- `LH760` – Lufthansa

---

