Android Studio Application: Developed with a group of five, followed SCRUM agile project management framework, utilized Jira and Git version control,conducted roughly 2 week sprints(3) over the course of around 2 months (3/3/2023-4/26/23), DB hosted on AWS.

Android App. designed to listen for the everyday usage of the word "Literally", 
-User Log-in/Create Account
-A foreground service is utilized to constanly record audio, this PCM audio is then put into a circularbytebuffer, which is then periodically sent to be transcribed using offline speech to text(Vosk-https://alphacephei.com/vosk/)
-If the word, "Literally" is detected, the date,time,location, is sent to the DB which is then fetched onto the main screen showcasing the report
