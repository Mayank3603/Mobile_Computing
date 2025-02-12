
# ✈️ Journey Tracker App

A simple **Android app** that allows users to **track their plane journey with multiple stops**, displaying **visa requirements, distance and time left between stops, and overall journey progress**. 

This project has **two versions**:

1. **Jetpack Compose Version** (`compose_version` branch) 🖌️
2. **XML + Kotlin Version** (`xml_version` branch) 📜

## 📌 Features

✅ **Read journey details from a text file** (`res/raw/testcase.txt`)  
✅ **Display journey stops with names, distances, and visa requirements**  
✅ **Show total distance and time remaining**  
✅ **Switch distance units between kilometers and miles**  
✅ **Progress bar to track journey completion**  
✅ **Next Stop button to update progress**  
✅ **Lazy List used for more than 3 stops**  
✅ **Works on both Android device and emulator**  

---

## 🚀 How to Install and Run

### **Clone the Repository**
```sh
git clone <your-repo-url>
cd <your-repo-name>

```
## Run the Compose Version
```sh
git checkout compose_version
```
## Run the XML Version
```sh

git checkout xml_version
```


## 🛠️ Jetpack Compose Version (`compose_version`)
This version **fully uses Jetpack Compose** for UI rendering, making it **declarative and modern**.

### **📂 Main Files**
- `JourneyApp.kt` - Handles main app logic
- `StopItem.kt` - Defines UI for a single stop
- `utils.kt` - Helper functions for distance conversion and formatting

### **📝 Lazy List Explanation**
- If the number of stops **exceeds 3**, a `LazyColumn` is used.
- **Why LazyColumn?**  
  Unlike a traditional list, `LazyColumn` **only renders visible items**, improving performance.


### 📱 UI Components Used
- Scaffold - Manages top bar, bottom navigation, etc.
- Column / Row - Arranges UI elements.
- Text - Displays stop details.
- Button - Used for "Next Stop" and "Switch Units".
- LinearProgressIndicator - Progress tracking.
 ## XML + Kotlin Version (xml_version)
This version uses traditional XML layouts and references UI elements in Kotlin using findViewById().

### 📂 Main Files
- activity_main.xml - Defines UI layout in XML.
- MainActivity.kt - Handles user interactions and updates UI.
- StopAdapter.kt - Adapter for RecyclerView to list stops.
- 📝 RecyclerView Explanation (Traditional List)
- If stops ≤ 3, they are shown in a LinearLayout.
- If stops > 3, RecyclerView is used for efficiency.

### 📱 UI Components Used
- RecyclerView - Handles dynamic lists.
- ProgressBar - Tracks journey progress.
- TextView / Button - Displays journey details.
- CardView - Styles each stop.
## 🔧 How to Use the App
- 📂 Load stops from res/raw/testcase.txt.
- 🔄 Switch between km & miles with "Switch Units" button.
- 🏁 Mark next stop with "Next Stop" button.
- 📊 Track progress using the progress bar.
- 🔄 Reset journey with the "Reset" button.
