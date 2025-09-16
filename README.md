🌿 GrowFit – Android Fitness Tracker

A clean, lightweight fitness app built with Kotlin + Jetpack Compose + Firebase to track your workouts.

✨ Features

🔐 Auth: Email/password Sign Up / Sign In, auto-login, logout.

📅 Plan: Weekly workout plan — add, edit, delete.

🏋️ Journal: Log workouts (type, duration, weight, notes) + optional photo.

📈 Progress: Line chart by exercise, metric (max weight/total duration), and days (7/30/90).

🎨 UX: Soft green Material 3 theme, smooth navigation, RTL support, empty states, form validation.

🚀 Quick Start

Open in Android Studio Koala/Iguana+ (JDK 17).

Add your Firebase google-services.json (matching package, e.g., com.example.growfit1).

Sync Gradle → ▶ Run on API 30+ device/emulator.

Enable Firebase Email/Password Auth + publish the rules below.

🔧 Firebase Rules

Firestore:

rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /users/{uid}/{doc=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}


Storage (optional photos):

rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /journal/{uid}/{all=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}


(Photos need Storage/Blaze plan, but journal entries work without them.)

🧭 How to Use

➕ Create an account or sign in.

📅 Plan → add/edit/delete workouts.

🏋️ Track → log a workout (+ photo) → Save.

📓 Journal → view/edit/delete entries.

📈 Progress → select exercise/metric/days → refresh.

🔓 Logout from the top bar.

🛠️ Troubleshooting

PERMISSION_DENIED: Publish rules & check you’re logged in.

Storage errors: Storage disabled/Blaze needed (photos optional).

JVM mismatch: Use Java 17 and set kotlinOptions.jvmTarget = "17".
