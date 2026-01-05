# Firebase Security Rules

To ensure the security and integrity of the manual review process, please update your Firebase security rules as follows:

## Firestore Rules
Update your `firestore.rules` file or copy-paste into the Firebase Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // ... existing rules ...

    match /driver_applications/{userId} {
      // Users can read their own application status
      allow read: if request.auth != null && request.auth.uid == userId;
      
      // Users can create their own application, but MUST set status to 'under_review'
      allow create: if request.auth != null && request.auth.uid == userId 
                     && request.resource.data.status == 'under_review';
      
      // Users cannot update the application once submitted (only admin can via console)
      // Exception: If you want to allow resubmission, you can check if status is 'rejected'
      allow update: if request.auth != null && request.auth.uid == userId 
                     && resource.data.status == 'rejected'
                     && request.resource.data.status == 'under_review';
      
      // Admins (or anyone with custom claims if implemented) can do anything
      // In this setup, "admin work" is done via the Console which bypasses rules
    }

    match /users/{userId} {
      allow read: if request.auth != null;
      // Prevent users from changing their own role
      allow update: if request.auth != null && request.auth.uid == userId 
                     && !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role_driver', 'role_customer']);
    }
  }
}
```

## Storage Rules
Update your `storage.rules` file or copy-paste into the Firebase Console:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // ... existing rules ...

    match /driver_applications/{userId}/{allPaths=**} {
      // Users can upload to their own folder
      allow write: if request.auth != null && request.auth.uid == userId;
      
      // Users can read their own documents
      allow read: if request.auth != null && request.auth.uid == userId;
      
      // Note: Admin console bypasses these rules for viewing
    }
  }
}
```
