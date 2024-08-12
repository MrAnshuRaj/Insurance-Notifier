# Insurance Notifier


**Insurance Notifier** is an Android application that helps users manage their insurance policies by notifying them about upcoming renewal dates. The app scans PDF documents to extract important information like policy names, renewal dates, and more. It stores this data in Firebase Firestore and SharedPreferences, and sends timely reminders as notifications.

## Features

- **PDF Scanning**: Select a PDF from device storage, extract the policy name, renewal date, and generate a thumbnail.
- **Firebase Integration**: Store user information and policy details in Firebase Firestore.
- **Notification System**: Get reminders 7 days, 3 days, and 1 day before the renewal date.
- **Foreground Service**: Runs in the background even when the app is closed, ensuring notifications are sent on time.
- **Automatic Start on Boot**: The app's notification service starts automatically when the device boots up.

## Getting Started

### Prerequisites

- Android Studio
- An Android device or emulator running Android 6.0 (Marshmallow) or higher
- A Firebase project with Firestore and Authentication set up

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/insurance-notifier.git
   cd insurance-notifier
   ```

2. **Set Up Firebase**:
   - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app to your Firebase project and download the `google-services.json` file.
   - Place the `google-services.json` file in the `app` directory of your Android project.

3. **Enable Firebase Authentication**:
   - In the Firebase Console, go to the Authentication section.
   - Enable Email/Password sign-in method.

4. **Enable Firestore Database**:
   - In the Firebase Console, go to the Firestore Database section.
   - Create a Firestore database with appropriate rules to allow reads/writes for authenticated users.

5. **Build and Run the App**:
   - Open the project in Android Studio.
   - Sync the project with Gradle files.
   - Build and run the app on your device or emulator.

## Usage

1. **Sign Up**:
   - Create a new account using your email and password.

2. **Sign In**:
   - Log in using your credentials.

3. **Add a New Insurance Policy**:
   - Select a PDF file containing your insurance policy.
   - The app will automatically extract the policy name, renewal date, and create a thumbnail.

4. **View Policies**:
   - The home page displays a list of your insurance policies with details like renewal date and days remaining.

5. **Receive Notifications**:
   - The app will notify you 7 days, 3 days, and 1 day before your policy's renewal date.

## Permissions

The app requires the following permissions:

- **Storage Access**: To select and read PDF files from your device.
- **Foreground Service**: To run the notification service in the background.
- **Boot Completed**: To start the notification service automatically when the device boots.
- **Post Notifications**: To display notifications.



## Acknowledgments

- **iText PDF Library**: Used for parsing PDF files.
- **Firebase**: For cloud storage and authentication.
- **Android Jetpack Components**: For efficient and effective Android development.

