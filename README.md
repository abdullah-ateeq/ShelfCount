# ShelfCount 📦📊

**ShelfCount** is a professional shelf capacity and layout micro-utility for Android. Designed for retail store planners, warehouse managers, inventory handlers, library curators, and home organizers, ShelfCount computes exact physical shelf capacity, margin clearances, utilization percentages, and dynamic 2D visual layouts in real time.

---

## ✨ Features

- 📏 **High-Precision Capacity Calculations**: Instant live calculation of items per shelf level and total multi-shelf capacity using arbitrary-precision arithmetic (`BigDecimal`).
- ↔️ **Margin & Depth Clearance Validation**: Configure left and right edge margins and validate front-to-back depth clearances (`item depth + margin ≤ shelf depth`).
- 🎨 **Interactive 2D Visual Shelf Representation**: Live preview showing placed items, gaps, margins, remaining space, and fit status badges (*Perfect Fit*, *Space Remaining*, *No Fit*).
- 🧮 **Step-by-Step Mathematical Breakdown**: Transparent educational calculation steps explaining why $N$ items fit and why $N+1$ items exceed available width.
- 📐 **Multi-Unit Engine**: Seamless live conversion between **Millimeters (mm)**, **Centimeters (cm)**, **Meters (m)**, **Inches (in)**, and **Feet (ft)**.
- 💾 **History & Data Persistence**: Save calculation presets with custom titles, item names, and notes. Filter, search, sort, reload into calculator, copy, share, or export to **JSON** and **CSV**.
- ⚙️ **Tailored Experience**: Material 3 design with Light, Dark, System Theme, Material You Dynamic Color, haptic feedback, configurable defaults, and toggleable display sections.
- 🔒 **100% Offline & Private**: Zero tracking, no ads, no internet permissions required.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose with Material 3 Design System
- **Navigation**: Navigation Compose
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF) with `StateFlow`
- **Data Persistence**: Jetpack DataStore Preferences
- **Math Engine**: Pure Kotlin `BigDecimal` calculator ensuring zero floating-point rounding errors
- **Build System**: Gradle with Kotlin DSL & Version Catalog (`libs.versions.toml`)
- **Min SDK**: 24 (Android 7.0) | **Target SDK**: 37 (Android 15)

---

## 📂 Project Structure

```text
com.example.shelfcount
├── calculator/      # Core mathematical calculation & unit conversion engine
├── data/            # DataStore preferences & history serialization repository
├── model/           # Data models (Inputs, Results, SavedCalculations, Units, Settings)
├── ui/
│   ├── components/  # Reusable Compose UI cards, formula sections, & 2D visualization
│   ├── navigation/  # Navigation bar & destinations setup
│   ├── screens/     # Calculator, History, Settings, About, and Splash screens
│   └── theme/       # Material 3 Color schemes, Type scale, and Theme setup
└── viewmodel/       # UI State management & ViewModel factory
```

---

## 🧪 Testing

The core calculation logic and history management are backed by unit tests:

```bash
# Run unit tests
./gradlew testDebugUnitTest
```

---

## 🚀 Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/ShelfCount.git
   cd ShelfCount
   ```

2. **Open in Android Studio**:
   - Open Android Studio (Ladybug 2024.2.1 or newer recommended).
   - Sync Gradle project.

3. **Build & Run**:
   - Select the `app` module and run on an Android emulator or physical device running Android 7.0 (API 24) or higher.
   - Alternatively, build debug APK via command line:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 👤 Author

Developed by **Abdullah Ateeq**  
GitHub: [@abdullah-ateeq](https://github.com/abdullah-ateeq)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
