# 🚗 GoUKM

### Campus Ride-Hailing Mobile Application

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Google Maps](https://img.shields.io/badge/Google%20Maps-4285F4?style=flat-square&logo=googlemaps&logoColor=white)

---

## 📌 Project Overview

**GoUKM** is a peer-to-peer ride-hailing platform designed for Universiti Kebangsaan Malaysia (UKM) students and staff. The application connects passengers seeking rides with verified drivers within the campus community, providing a safe, affordable, and convenient alternative to traditional transportation.

The platform enables users to flexibly switch between **Passenger** and **Driver** roles, accommodating the diverse mobility needs of the university community.

**Platform:** Android (Kotlin + Jetpack Compose)

---

## 🎯 Problem

Campus transportation is fragmented. Students manually coordinate carpools through social media, making the process time-consuming, unsafe, and unreliable. Current solutions lack:

- **Centralized coordination** – No single platform for ride requests
- **Safety verification** – Unverified drivers and passengers
- **Trust mechanism** – No rating or accountability system  
- **Transparency** – Unclear pricing and unclear driver information
- **Accessibility** – No structured driver registration

These gaps create inefficiency and limit participation.

---

## 💡 Solution

GoUKM provides a unified, app-based ride-hailing ecosystem with:

1. **Dual-role system** – Users flexibly switch between passenger and driver
2. **Driver verification** – Document upload, background checks, and admin approval
3. **Real-time ride matching** – Immediate ride request broadcasts to available drivers
4. **Fare transparency** – Drivers propose fares; passengers choose preferred rates
5. **Trust & accountability** – Integrated rating and review system
6. **Real-time communication** – In-app chat and call for coordination
7. **Payment flexibility** – Cash and DuitNow QR payment options
8. **Driver analytics** – Earnings tracking, productivity insights, and demand patterns

---

## ✨ Key Features

### 👤 **Passenger Features**

- **Account Management**
  - Email-based registration and authentication
  - Profile creation with personal details
  - Profile editing and photo upload

- **Ride Booking**
  - Pickup and destination selection via Google Maps
  - Real-time seat capacity selection
  - Ride status tracking (Pending → Offered → Accepted → Ongoing → Completed)

- **Fare Selection**
  - View multiple driver fare offers for a single request
  - Compare driver profiles (rating, vehicle, vehicle plate)
  - Select preferred fare and driver

- **Payment**
  - Two payment methods: Cash or DuitNow QR
  - Payment confirmation before ride start
  - Payment status tracking

- **Communication**
  - Real-time chat with assigned driver
  - Phone call capability
  - Message read status

- **Feedback & Ratings**
  - Post-ride rating and comment system (1–5 stars)
  - Automatic driver rating recalculation
  - One rating per ride

- **Booking History**
  - View all past rides with full details
  - Search and filter rides by category
  - Access driver contact info from history

- **Driver Upgrade**
  - Apply to become a driver
  - Eligibility checks and application workflow

### 🚗 **Driver Features**

- **Account & Verification**
  - Driver application form submission
  - Multi-document upload (IC, license, insurance, bank QR)
  - Image compression for storage optimization
  - Application status tracking (Pending → Under Review → Approved/Rejected)

- **Vehicle Registration**
  - Vehicle details form (brand, color, plate number, vehicle type)
  - Real-time status updates during application review
  - Rejection feedback with ability to reapply

- **Availability Management**
  - Toggle availability switch on driver dashboard
  - Real-time database updates
  - Automatic ride request filtering based on status

- **Ride Management**
  - Incoming ride request cards (pickup, destination, passenger info)
  - Offer/Skip functionality for request filtering
  - Fare offer submission with validation (min/max logic)
  - Real-time driver location broadcast
  - Journey completion workflow

- **Driver Navigation**
  - Google Maps navigation to pickup location
  - Real-time location tracking during ride
  - Arrival confirmation at pickup and dropoff

- **Earnings Dashboard**
  - Total earnings display with currency formatting
  - Income aggregation by day, month, or year
  - Date range filtering
  - Empty state handling for zero-earnings periods
  - Historical data visualization

- **Performance Analytics**
  - Overall rating display and average calculation
  - "No ratings yet" messaging for new drivers
  - Total completed rides counter
  - Total driving hours in selected period
  - Hourly ride demand bar chart with peak hour highlighting
  - Real-time metrics for productivity analysis

- **Ride History**
  - View past completed rides
  - Passenger details and ride information
  - Search and filter options

### 🔔 **Notifications**

- **Push Notifications (FCM)**
  - New ride request alerts to available drivers
  - Fare offer notifications to passengers
  - Offer acceptance notifications to drivers
  - Driver application approval/rejection notifications
  - Real-time in-app + system tray delivery

- **Notification Types**
  - Ride request broadcast
  - New fare offer received
  - Offer accepted confirmation
  - Application approval/rejection

### 📍 **Maps & Location**

- **Google Maps Integration**
  - Pickup and destination selection
  - Places API for address autocomplete
  - Location coordinate capture (latitude/longitude)
  - Navigation to pickup location (driver)
  - Route visualization on map
  - Real-time driver position updates

- **Location Permissions**
  - Fine and coarse location access requests
  - Runtime permission handling

---

## 🔄 Core User Workflow

### Passenger Journey

```
Register/Login
         ↓
Select Role (Passenger)
         ↓
Search Ride Request
         ↓
Enter Pickup & Destination
         ↓
Select Seats & Payment Method
         ↓
Submit Ride Request (Broadcast)
         ↓
View Driver Offers
         ↓
Select Driver & Accept Offer
         ↓
Chat/Call with Driver
         ↓
Complete Journey
         ↓
Rate Driver & Provide Feedback
         ↓
View Booking History
```

### Driver Journey

```
Register/Login
         ↓
Select Role (Driver)
         ↓
Submit Driver Application
         ↓
Upload Verification Documents
         ↓
Admin Review & Approval
         ↓
Register Vehicle Details
         ↓
Enable Availability
         ↓
Receive Ride Requests (Filtered)
         ↓
Submit Fare Offer
         ↓
Accept Confirmed Ride
         ↓
Navigate to Pickup
         ↓
Complete Journey
         ↓
View Earnings & Performance Metrics
```

---

## 👥 User Roles

| Role | Responsibilities |
|------|-------------------|
| **Passenger** | Search rides, request bookings, pay drivers, rate drivers, view history |
| **Driver** | Complete verification, manage availability, respond to requests, submit fares, navigate rides, track earnings |
| **Admin** | Review driver applications, approve/reject drivers, manage system (backend) |

---
## 🛠️ Tech Stack

| Category | Technologies |
|----------|---------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM (ViewModels, StateFlow, Repositories) |
| **Authentication** | Firebase Authentication (Email) |
| **Database** | Cloud Firestore (Real-time NoSQL) |
| **File Storage** | Firebase Storage (Documents, images) |
| **Push Notifications** | Firebase Cloud Messaging (FCM) |
| **Server-side Logic** | Firebase Cloud Functions (Node.js) |
| **Maps & Location** | Google Maps SDK, Google Places API |
| **Camera & File Access** | AndroidX Camera, MediaStore |
| **Asynchronous** | Kotlin Coroutines, Flow |
| **Image Loading** | Coil Compose |
| **HTTP Client** | OkHttp |
| **Build System** | Gradle (Kotlin DSL) |
| **Target SDK** | Android 35 (API level 35) |
| **Min SDK** | Android 7.0 (API level 24) |

---

## 🎓 Project Context

**GoUKM** was developed as a **team software engineering project** at Universiti Kebangsaan Malaysia (UKM) during the Software Development course. The project involved three team members collaborating over multiple sprints to design, build, and deliver a fully functional mobile application from requirements to production.

The project demonstrated:
- Full-stack mobile application development
- Distributed system design with Firebase
- Team collaboration and project management
- End-to-end software engineering lifecycle

---

## 🚧 Project Status

✅ **Completed as an academic software engineering project.**

The application has successfully implemented:
- All core features for passengers and drivers
- Complete ride-hailing workflow
- Real-time notifications and messaging
- Driver verification and earnings tracking
- Payment integration

**Deployment Status:** Developed and tested on Android devices (min API 24, target API 35)

---

## 🤝 Team

| Role | Name | GitHub |
|------|------|--------|
| **Project Leader & Core Developer** | Siti Farhana Binti Marzuki | [@panabijak](https://github.com/panabijak) |
| **Developer** | Azrai | [@AzureeKun](https://github.com/AzureeKun) |
| **Developer** | Izzah | [@izzahrhnh](https://github.com/izzahrhnh) |
