# SoundBoard API
A RESTful API for managing sound files for the purpose of triggering sound effects on command
or playing longer form audio files. The API allows users to upload, manage, and trigger sound files through HTTP requests.

# Architecture
The SoundBoard API is built using Spring-Boot and is designed to be modular and scalable. It consists of several key components:
- **Controllers**: Handle incoming HTTP requests and route them to the appropriate services.
- **Services**: Contain the business logic for managing sound files, including uploading, retrieving, and triggering sounds.
- **Repositories**: Interact with the database to store and retrieve sound file metadata and user information.
- **Models**: Define the data structures for sound files, users, and other relevant entities.
