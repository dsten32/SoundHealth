# SoundHealth
PfR2 Project.

Masters paper project app

App is intended to collect location, date/time and, decibel information either when the user 
manually triggers collection of via a scheduled periodic recording.
This data is synced to google Firestore, and is veiwable either as a heatmap on a googlemaps view or in piechart form.
Piechart is user data only, heatmap is either user data or all data collected in the firestore collection.

Users can choose the interval and/or stop time for scheduled data collection. 
To address privacy concerns location data can also be perturbed (Blurred) by a factor specified by the user.
The user can choose a blur factor betweem 0.1 km and 0.8 km.

The user can specify filters for the heatmap visualisation. The user can choose to display data for specific days 
of the week, time ranges and/or date ranges.
