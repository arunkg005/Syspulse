# 1.App & System Architecture (Backend Flow)

## 🎯 Objective

Define a clean backenddata flow architecture where

 DeviceInfoActivity = Entry Screen
 No separate MainActivity
 Focus is on how data flows across modules, not UI navigation

---

# 🏗️ High-Level Architecture (Backend-Oriented)

```text
[ System APIs ]        [ Sensor APIs ]        [ User Input ]
       ↓                     ↓                     ↓
-------------------------------------------------------------
                Core Data Layer (Collectors)
-------------------------------------------------------------
       ↓                     ↓                     ↓
         Processing  Interpretation Layer
-------------------------------------------------------------
                         ↓
                UI Layer (Activities)
```

---

# 📱 Activities (UI Layer Only)

```text
DeviceInfoActivity (Entry Point)
    ├── SensorLabActivity
    └── ReportActivity
```

---

# 🧠 Core Backend Modules

## 1. SystemInfoCollector

### Purpose

Collect all static and semi-static device data

### Sources

 Build.
 ActivityManager
 StatFs
 BatteryManager
 ConnectivityManager

---

## 2. SensorDataCollector

### Purpose

Handle real-time and snapshot sensor data

### Responsibilities

 Get available sensors
 Registerunregister listeners
 Provide

   Live stream (SensorLab)
   Snapshot (Report)

---

## 3. ReportDataCollector

### Purpose

Aggregate all data into single structured object

### Input

 SystemInfoCollector
 SensorDataCollector
 UserTestResults

---

## 4. UserTestManager

### Purpose

Handle interactive diagnostics

### Tests

 Display
 Touch
 Audio
 Buttons

---

## 5. ReportBuilder

### Purpose

Convert structured data → readable report

---

## 6. Logger (Optional MVP+)

### Purpose

Store generated reports

---

# 🔄 Data Flow (End-to-End)

```text
[ DeviceInfoActivity Load ]
        ↓
SystemInfoCollector.fetch()
        ↓
Display basic info

------------------------------------

[ SensorLabActivity ]
        ↓
SensorDataCollector.startListening()
        ↓
Live sensor stream → UI

------------------------------------

[ ReportActivity ]
        ↓
Trigger ReportDataCollector
        ↓
   SystemInfoCollector.fetch()
 + SensorDataCollector.snapshot()
 + UserTestManager.runTests()
        ↓
ReportBuilder.generate()
        ↓
Display + Save
```

---

# 🧩 Module Interaction

```text
SensorLabActivity
    ↔ SensorDataCollector

ReportActivity
    ↔ SystemInfoCollector
    ↔ SensorDataCollector
    ↔ UserTestManager
    → ReportBuilder
    → Logger
```

---

# ⚙️ Data Ownership Rules

 Module               Owns Data      Notes             
 -------------------  -------------  ----------------- 
 SystemInfoCollector  Device info    Static snapshot   
 SensorDataCollector  Sensor values  Live + snapshot   
 UserTestManager      Test results   Event-based       
 ReportBuilder        Final output   No raw collection 

---

# 🔧 Communication Pattern

## Direct Method Calls (MVP)

No need for

 ViewModel
 LiveData
 Dependency Injection

---

## Example Flow

```java
SystemInfo info = SystemInfoCollector.getInfo();
ListSensorSnapshot sensors = SensorDataCollector.getSnapshot();
ListTestResult tests = UserTestManager.run();

String report = ReportBuilder.build(info, sensors, tests);
```

---

# ⏱️ Execution Strategy

## DeviceInfoActivity

 Fetch once on load

## SensorLabActivity

 Continuous updates (listener-based)

## ReportActivity

 One-time execution per request

---

# ⚠️ Constraints (Strict MVP Discipline)

 No background services
 No database (file optional)
 No async frameworks (basic ThreadHandler only)
 No caching layer

---

# 🧠 Error Handling Flow

```text
If API fails → return null
        ↓
UI shows Not Available
```

---

# 🔄 Lifecycle Integration

## SensorDataCollector

```text
onResume → startListening()
onPause → stopListening()
```

---

## Report Flow

```text
Button Click → Collect → Build → Display
```

---

# 🔥 Key Design Principles

1. Separation of Concerns

    Collection ≠ Processing ≠ Display

2. Single Source of Truth

    Each module owns its data

3. Stateless Processing

    No unnecessary persistence

4. Fail Gracefully

    Missing data handled at UI level

---

# 📌 Final Backend Flow Summary

```text
Collect → Aggregate → Interpret → Format → Display
```

---

# 🚀 Outcome

 Clean backend structure
 Easy to debug
 Easy to extend later
 Fast to implement in Java

---

Now your architecture matches your actual UX

 Entry = Device Info
 Backend = modular + reusable
