# Missing Person Android App

A comprehensive Android application designed to assist in locating missing persons by allowing users to report sightings, upload images, and search through existing reports.
The app provides a user-friendly interface with real-time map updates, facilitating efficient communication and data sharing among users.

## Features

- **Multiple Fragments:** Home, Search, My Report, and Profile for easy navigation.
- **Report Missing Persons:** Submit detailed reports with images and descriptions of missing individuals.
- **Real-time Updates:** View live updates of reported sightings on Google Maps.
- **User Interaction:** Contact reporters directly through the app to provide chat functionality.
- **Firebase Integration:** Utilizes Firebase Realtime Database for efficient data management and Google Maps SDK for map functionality.

## Prerequisites
This project uses the OpenCV library for image processing and computer vision tasks. To run this project, you will need to set up OpenCV for Android.

- Installing OpenCV
Add OpenCV Dependency: Include OpenCV as a dependency in your build.gradle file:
*gradle
Copy code
dependencies {
    implementation 'org.opencv:opencv-android:4.5.5'  // Replace with the version you need

- Usage in the Project
OpenCV is used in this project for image processing tasks, such as:
Face Detection: Using OpenCV's pre-trained Haar cascades for real-time face detection.

- Further Reading
OpenCV for Android Documentation
OpenCV API Reference

## Screenshots

1. **Login Activity**, **Create New Account Activity**, and **Home Fragment**  
   <div style="display: flex; justify-content: space-between;">
      <img src="https://github.com/user-attachments/assets/9d77d7ec-2f0a-4773-86b9-8c106e753d31" alt="Login_Activity" width="200">
      <img src="https://github.com/user-attachments/assets/32dd76dc-7123-46f9-a1c3-857b6038aec3" alt="CreateAccount_activity" width="200">
      <img src="https://github.com/user-attachments/assets/48783baa-3039-42a7-9b09-a6b9ca1571ac" alt="Home_Fragment" width="200">
   </div>

2. **Add Report Activity**, **Marker Details Activity**, and **Search Fragment**  
   <div style="display: flex; justify-content: space-between;">
      <img src="https://github.com/user-attachments/assets/fa9a9f0b-1d5e-442f-a341-ef4eae55d9f1" alt="FloatingB_addReport_Activity" width="200">
      <img src="https://github.com/user-attachments/assets/b9d03599-2ccb-4b71-819e-a6909439463f" alt="Markerclick_details" width="200">
      <img src="https://github.com/user-attachments/assets/bbb131dc-ce01-4eae-b32d-7449153dd60c" alt="Search_Fragment" width="200">
   </div>

3. **Chat List Fragment**, **Chat Activity**, and **My Reports Fragment**  
   <div style="display: flex; justify-content: space-between;">
      <img src="https://github.com/user-attachments/assets/a1540dc9-82e8-43ea-ab43-31759249b3ec" alt="User_Chatlist" width="200">
      <img src="https://github.com/user-attachments/assets/b6e2a1ff-1bda-4340-98a7-1798a0242aa9" alt="Chat_Activity" width="200">
      <img src="https://github.com/user-attachments/assets/b2ef805b-ceb7-4522-9e81-a0d9448bfc13" alt="Reports_Fragment" width="200">
   </div>

4. **Reports Updated Details Activity** and **Profile Fragment**  
   <div style="display: flex; justify-content: space-between;">
      <img src="https://github.com/user-attachments/assets/c7cd556b-8fed-4f37-8c0b-d6e1e6f15c09" alt="Reports_updated_details" width="200">
      <img src="https://github.com/user-attachments/assets/0906836d-51e5-4a48-92be-77beee2d2ff8" alt="Profile_Fragment" width="200">
   </div>




## Usage

1. **Create an Account:**
   - Open the app and create a new account by providing basic details.

2. **Log In:**
   - Use your credentials to log in and access the home screen.

3. **Report a Missing Person:**
   - Navigate to the 'Home' fragment and use the floating button to create a new report with relevant details.

4. **Search for Reports:**
   - Go to the 'Search' fragment and look for reports using various filters.

5. **View and Edit Your Reports:**
   - Access the 'My Report' fragment to view updated regarding reports.

6. **Update Profile or Logout:**
   - Manage your profile details or log out using the 'Profile' fragment.

## Architecture and Technical Details

- **Frontend:** Android SDK, Java
- **Backend:** Firebase Realtime Database
- **Map Integration:** Google Maps SDK
- **UI Components:** Activities, Fragments, RecyclerView, and various UI elements for a seamless user experience.

## Download

You can download the latest version of the app from the link below:

## Acknowledgements

- [Firebase](https://firebase.google.com/) for Realtime Database and Authentication.
- [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/overview) for map integration.
- Inspiration and resources from the Android development community.

## Contact

If you have any questions or feedback, feel free to reach out at [sawantvishal2001@gmail.com]

---

### Additional Notes

- This app was built as part of my personal project to help reunite missing persons with their families.
- I would love to hear from you if you have suggestions or improvements!
