# InstaPost
Assignment for CS 646 Android app development. Create a simplified instagram clone.

Requirements for the assignment:

1. Use FireBase realtime database for storing user information and Fire Cloud storage
for storing images and other content
2. Each user must be able to see a list of other users and see their posts
3. Each user must be able to see a list of hashtags and see all the posts tagged by that 
hashtag.

Approach used:

1. Built 4 fragments for creating post, users list, hashtags list and dashboard respectively
2. Signing up stores user info on realtime dataabase, and creates an FireBaseAuth instance with which
we manage user sessions
3. Create post uploads the photos to cloud storage and stores its URL on the realtime database for 
quick access.
4. The database has two tables: Users and HashTags. Each post made sends a postID in the Users and Hashtags table, which makes it easy to retrieve them all in the list.
5. Dashboard fragment simply loads all the posts for whichever user/hashtag is passed to it as a parameter.
6. Used Picasso for loading images, CardView for the elevated look, Groupie to group viewholders quickly and effectively. Also used AndroidX objects since they have better benefits.
