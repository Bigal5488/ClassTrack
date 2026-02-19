# How to Upload ClassTrack to GitHub

Since Git is not currently installed on your system (or not in your PATH), I couldn't automatically upload the files. Please follow these steps to upload your project to `https://github.com/Bigal5488/ClassTrack.git`.

## Step 1: Install Git
1.  Download Git from [git-scm.com](https://git-scm.com/downloads).
2.  Install it with default settings.
3.  **Important**: During installation, ensure the option "Git from the command line and also from 3rd-party software" is selected (it usually is by default).

## Step 2: Upload Project via Command Line (Recommended)
Once Git is installed, open your terminal (Command Prompt or PowerShell) in this folder (`c:\Users\Bigal\OneDrive\Documents\Aditya university\Projects\ClassTrack`) and run the following commands one by one:

```bash
# 1. Initialize Git repository
git init

# 2. Add all files to the staging area
git add .

# 3. Commit the changes
git commit -m "Initial commit of ClassTrack project"

# 4. Rename the branch to 'main'
git branch -M main

# 5. Link to your GitHub repository
git remote add origin https://github.com/Bigal5488/ClassTrack.git

# 6. Push the files to GitHub
git push -u origin main
```

## Step 3: Alternative - Upload via Web Browser
If you prefer not to use the command line:
1.  Go to your repository: [https://github.com/Bigal5488/ClassTrack](https://github.com/Bigal5488/ClassTrack).
2.  Click on **Add file** > **Upload files**.
3.  Drag and drop all files and folders from your project directory into the browser window.
    *   **Note**: You cannot drag the empty `bin` folder, but that is fine as it is auto-generated.
4.  Commit the changes.
