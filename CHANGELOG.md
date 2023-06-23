## 0.1
Initial Release

## 0.2
Reworked API and workflow to enable emulation of key attestation on iOS

### 0.3
Explicit `verifyKeyAttestation` function for both mobile platforms

### 0.3.1
- More Java-friendly API
- More detailed toplevel exception messages on certificate verification error (Android)
- Kotlin 1.8.0

### 0.3.2
- fixed iOS leeway calculation

### 0.3.3
- update upstream google code

## 0.4
- ability to ignore timely validity of leaf cert for Android key attestation

### 0.4.1
- bugfix: NOOP attestation service actually being a NOOP

## 0.5.0
- Group OS-specific interfaces
- Align exception types between iOS and Android
 
### 0.5.1
-  depend on android-attestation 0.8.3 (MR Jar)

### 0.5.2
- Kotlin 1.8.21
- Gradle 8.1.1
- depend on android-attestation 0.8.4 to support custom Android trust anchors and testing against software-created
  attestations.

### NEXT
- android-attestation updated
- use A-SIT Plus gradle conventions plugin
