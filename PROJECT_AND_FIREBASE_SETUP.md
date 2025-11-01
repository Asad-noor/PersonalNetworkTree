## 🚀 Quick Start Guide

### Prerequisites
Before you begin, ensure you have:
- **Android Studio** (Latest version - Arctic Fox or newer)
- **JDK 11** or higher
- **Active internet connection**
- **Google Account** (for Firebase setup)

### Step 1: Extract the Project
1. Extract the downloaded ZIP file to your desired location
2. Note the project folder path

### Step 2: Open in Android Studio
1. Launch **Android Studio**
2. Click **"Open"** (or File → Open)
3. Navigate to the extracted project folder
4. Select the **"PersonalNetworkTree"** folder
5. Click **"OK"**
6. Wait for Gradle sync to complete (this may take a few minutes)

## 🎓 Learning Resources

This project is excellent for learning:
- ✅ **Jetpack Compose** - Modern Android UI development
- ✅ **Firebase Integration** - Cloud backend services
- ✅ **MVVM Architecture** - Clean, maintainable code structure
- ✅ **Material Design 3** - Latest design patterns
- ✅ **Navigation Compose** - Modern navigation patterns
- ✅ **State Management** - StateFlow and reactive programming
- ✅ **Repository Pattern** - Data layer abstraction
- ✅ **Coroutines** - Asynchronous programming in Kotlin

## 🔧 Customization Guide

### Change App Name
1. Open `app/src/main/res/values/strings.xml`
2. Modify: `<string name="app_name">Your App Name</string>`

### Change Package Name
1. Right-click on package in Android Studio
2. Select "Refactor" → "Rename"
3. Update `applicationId` in `app/build.gradle.kts`
4. Update in Firebase Console settings

### Change App Theme/Colors
1. Open `app/src/main/java/.../ui/theme/Color.kt`
2. Modify color values
3. Update Material 3 color scheme in `Theme.kt`

### Change App Icon
1. Right-click on `res` folder → New → Image Asset
2. Select "Launcher Icons (Adaptive and Legacy)"
3. Choose your icon image
4. Configure foreground/background
5. Click "Next" → "Finish"


# 🔥 Firebase Setup Guide for Personal Network Tree App
## Complete Step-by-Step Guide (For Non-Technical Users)

## 📋 What You'll Need

Before starting, make sure you have:
- ✅ A Google account (Gmail)
- ✅ Internet connection
- ✅ 15-20 minutes of time
- ✅ The Android project folder

---

## 🎯 Overview: What is Firebase?

Firebase is Google's cloud service that stores your app's data (like contacts and notes) online. This allows:
- 📱 Your data to be saved even if you uninstall the app
- 🔄 Real-time updates across multiple devices
- 🔒 Secure user authentication

**Important:** Photos are stored on your device only (not in the cloud), which keeps the app completely free!

---

# Part 1: Create Your Firebase Project

## Step 1: Go to Firebase Console

1. Open your web browser
2. Go to: **https://console.firebase.google.com**
3. Click **"Sign in"** with your Google account
4. After signing in, you'll see the Firebase Console homepage

---

## Step 2: Create a New Project

1. Click the **"Create a project"** button (or **"Add project"** if you have existing projects)

2. **Enter Project Name:**
   - Type: `PersonalNetworkTree` (or any name you prefer)
   - Click **Continue**

3. **Google Analytics (Optional):**
   - You'll see "Enable Google Analytics for this project?"
   - **Toggle it OFF** (we don't need analytics)
   - Click **Create project**

4. **Wait for Project Creation:**
   - Firebase will set up your project (takes 10-30 seconds)
   - When you see "Your new project is ready", click **Continue**

---

## Step 3: Add Android App to Your Project

1. On your project homepage, you'll see options to add apps
2. Click the **Android icon** (looks like a robot)

3. **Register Your App:**
   - **Android package name:** Enter exactly what you have updated to the android app project like
   `com.example.personalnetworktree`
   - **App nickname (optional):** You can leave this blank
   - **Debug signing certificate (optional):** Leave blank for now
   - Click **Register app**

4. **Download google-services.json:**
   - You'll see a **"Download google-services.json"** button
   - Click it to download the file to your computer
   - **IMPORTANT:** Remember where you saved this file! You'll need it soon
   - Click **Next**

5. **Skip the SDK Setup:**
   - The next screens show code to add
   - You can click **Next** → **Next** → **Continue to console**
   - (The app code already has these configurations)

---

# Part 2: Enable Firebase Authentication

## Step 4: Set Up Email/Password Authentication

1. In your Firebase Console, look at the **left sidebar**
2. Find the **"Build"** section
3. Click on **"Authentication"**

4. **Get Started:**
   - If you see a **"Get started"** button, click it
   - This opens the Authentication dashboard

5. **Enable Sign-in Method:**
   - Click on the **"Sign-in method"** tab (at the top)
   - You'll see a list of sign-in providers

6. **Enable Email/Password:**
   - Find **"Email/Password"** in the list
   - Click on it
   - Toggle **"Enable"** to ON (should turn blue)
   - Click **"Save"**

✅ **Authentication is now set up!**

---

# Part 3: Enable Firebase Realtime Database

## Step 5: Create Realtime Database

1. In the **left sidebar**, under **"Build"** section
2. Click on **"Realtime Database"** 
   - ⚠️ **IMPORTANT:** Click "Realtime Database", NOT "Firestore Database"
   - They are two different services!

3. **Create Database:**
   - Click the **"Create Database"** button

4. **Choose Database Location:**
   - Select a location closest to your users
   - Examples:
     - `United States (us-central1)` - For US users
     - `Europe (europe-west1)` - For European users
     - `Asia (asia-southeast1)` - For Asian users
   - Click **Next**

5. **Set Security Rules:**
   - Select **"Start in locked mode"** (more secure)
   - Click **Enable**
   - Wait 10-20 seconds for the database to be created

✅ **Realtime Database is now created!**

---

## Step 6: Configure Database Security Rules

After the database is created, you need to set up security rules so only logged-in users can access their own data.

1. You should now see your Realtime Database page
2. At the top, click on the **"Rules"** tab

3. **Replace the Rules:**
   - You'll see existing rules that look like this:
   ```json
   {
     "rules": {
       ".read": false,
       ".write": false
     }
   }
   ```

4. **Delete everything** and replace it with these new rules:
   - Select all the text (Ctrl+A or Cmd+A)
   - Delete it
   - Copy and paste the following:

```json
{
  "rules": {
    "users": {
      "$userId": {
        ".read": "$userId === auth.uid",
        ".write": "$userId === auth.uid",
        "contacts": {
          ".indexOn": ["name", "relationshipLevel", "createdAt"]
        },
        "interactions": {
          ".indexOn": ["contactId", "date"]
        },
        "tags": {
          ".indexOn": ["name"]
        }
      }
    }
  }
}
```

5. Click the **"Publish"** button (top-right)
6. If you see a warning, click **"Publish"** again to confirm

✅ **Security rules are now configured!**

### 📖 What These Rules Mean (Simple Explanation):
- Users can only see and edit **their own data**
- Each user's data is stored separately
- Nobody can access another user's contacts
- Authentication is required to use the app

---

# Part 4: Replace google-services.json in Android Project

## Step 7: Add the Configuration File to Your Android Project

Now you need to add the `google-services.json` file you downloaded earlier into the Android project.

### 🔍 Find Your Downloaded File:
1. Go to your computer's **Downloads** folder
2. Look for a file named: `google-services.json`
3. If you can't find it:
   - Go back to Firebase Console
   - Click the **gear icon** (⚙️) next to "Project Overview" in the left sidebar
   - Click **"Project settings"**
   - Scroll down to "Your apps" section
   - Click the **download icon** next to your Android app to download again

### 📁 Copy File to Android Project:

1. **Locate Your Project Folder:**
   - Open the folder where you have the Android project
   - You should see folders like: `app`, `gradle`, and files like `build.gradle.kts`

2. **Navigate to the app Folder:**
   - Open the `app` folder
   - You should see folders like: `src`, `build`, and a file `build.gradle.kts`

3. **Replace the File:**
   - **If there's an OLD `google-services.json` file in this folder:**
     - Delete it first
   - **Copy your NEW `google-services.json` file** into this `app` folder
   - The file should be directly inside the `app` folder

### ✅ Correct File Location:
```
PersonalNetworkTree/
├── app/
│   ├── google-services.json    ← YOUR FILE SHOULD BE HERE
│   ├── build.gradle.kts
│   └── src/
├── gradle/
└── build.gradle.kts
```

---

# Part 5: Test Your Setup

## Step 8: Build and Run the App

1. **Open the Project in Android Studio:**
   - Open Android Studio
   - Click **"Open"** and select the project folder
   - Wait for Gradle sync to complete

2. **Build the Project:**
   - Click **Build** menu → **Rebuild Project**
   - Wait for the build to finish (may take 1-2 minutes first time)
   - Check for any errors in the "Build" tab at the bottom

3. **Run on Device or Emulator:**
   - Connect your Android phone via USB (with USB debugging enabled)
   - OR use an Android Emulator
   - Click the **green play button** (▶️) at the top
   - Select your device
   - Click **OK**

---

## Step 9: Test the App Features

### Test 1: Sign Up
1. App opens to Sign Up screen
2. Enter a test email: `test@example.com`
3. Enter a password: `Test123456`
4. Click **Sign Up**
5. ✅ Should see "Sign up successful!"

### Test 2: Add a Contact
1. After signing in, you'll see the Network Graph (only one node - "Me")
2. Tap the **+ button** (bottom-right corner)
3. Fill in:
   - Name: `John Doe`
   - Email: `john@example.com` (optional)
   - Phone: `+1234567890` (optional)
4. Tap the circular photo area to add a photo (optional)
5. Tap **SAVE**
6. ✅ Should see "Contact added successfully!"
7. ✅ Should see John appear in the network graph

### Test 3: Verify Data in Firebase
1. Go back to Firebase Console
2. Click **Realtime Database** in the left sidebar
3. Click the **Data** tab
4. You should see:
   ```
   users
     └── (some random ID - your user ID)
         └── contacts
             └── (some random ID - contact ID)
                 ├── name: "John Doe"
                 ├── email: "john@example.com"
                 └── ... (other fields)
   ```

✅ **If you see this data, everything is working perfectly!**

---

# 🎉 Setup Complete!

## 🔒 Important Notes About Privacy & Storage

### What's Stored in Firebase (Cloud):
- ✅ Contact names, emails, phones
- ✅ Company and position info
- ✅ Tags and notes
- ✅ Interaction history
- ✅ Relationship levels

### What's Stored Locally (On Device Only):
- 📸 **Contact Photos** - Never uploaded to cloud
- 🔐 Photos remain private on your device
- 💰 This keeps the app **100% FREE** (no storage costs)

### Data Security:
- 🔒 Each user can only access their own data
- 🔒 Passwords are encrypted
- 🔒 Data transmission is secure (HTTPS)
- 🔒 Nobody else can see your contacts

---

## ❓ Troubleshooting Common Issues

### Issue 1: "App not registered" Error
**Solution:**
- Check that `google-services.json` is in the correct location (`app` folder)
- Verify the package name matches with your given package name in the project
- Rebuild the project

### Issue 2: "Authentication failed" Error
**Solution:**
- Go to Firebase Console → Authentication
- Verify Email/Password is **Enabled**
- Check your internet connection

### Issue 3: "Permission denied" Error
**Solution:**
- Go to Firebase Console → Realtime Database → Rules
- Verify the rules are exactly as shown in Step 6
- Click **Publish** again

### Issue 4: Can't See Data in Firebase
**Solution:**
- Make sure you signed up/logged in first
- Add at least one contact
- Refresh the Firebase Console page
- Check the "Data" tab (not "Rules" tab)

### Issue 5: Build Errors in Android Studio
**Solution:**
- Make sure `google-services.json` is in the `app` folder
- Click **File** → **Sync Project with Gradle Files**
- Click **Build** → **Clean Project**
- Click **Build** → **Rebuild Project**

---

## 📞 Need More Help?

### Useful Firebase Documentation:
- Firebase Console: https://console.firebase.google.com
- Firebase Authentication Docs: https://firebase.google.com/docs/auth
- Realtime Database Docs: https://firebase.google.com/docs/database

### Checking Your Firebase Usage (Stay in Free Tier):
1. Go to Firebase Console
2. Click **Usage and billing** in left sidebar
3. You'll see:
   - Authentication: Unlimited users (free)
   - Realtime Database: 1 GB storage (free)
   - Downloads: 10 GB/month (free)

**For this app, you'll stay well within free limits!**

---

## ✅ Final Checklist

Before delivering to your client, ensure:

- [ ] Firebase project is created
- [ ] Authentication is enabled (Email/Password)
- [ ] Realtime Database is created
- [ ] Database security rules are configured and published
- [ ] `google-services.json` file is in the `app` folder
- [ ] Project builds without errors
- [ ] App can sign up new users
- [ ] App can add contacts
- [ ] Data appears in Firebase Console
- [ ] Network graph displays contacts correctly

---

## 🎯 Quick Reference Card (For Your Client)

**Firebase Console URL:**  
https://console.firebase.google.com

**File to Replace:**  
`app/google-services.json`

**Free Tier Limits:**
- Users: Unlimited ✅
- Database: 1 GB ✅
- Downloads: 10 GB/month ✅

**No Credit Card Required!** ✅

---

**Setup Guide Version:** 1.0  
**Last Updated:** October 31, 2025  
**App:** Personal Network Tree  

---

End of Setup Guide 🎉

