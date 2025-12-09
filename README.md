# 🎭 Who Is The Spy

A multiplayer web application for playing "Who Is The Spy" with AI-generated images using Spring Boot and React.

## 🚀 Features

- **Multiplayer Game**: Create or join games with a 6-letter join code
- **AI Image Generation**: Automatically generate images using OpenAI's DALL-E via Spring AI
- **Real-time Updates**: WebSocket/STOMP for instant game state and image updates
- **Role Assignment**: Automatic spy/civilian role assignment with different words
- **Host Controls**: Host can start games, advance turns, and generate images
- **Modern UI**: Beautiful React frontend with TypeScript

## 📋 Prerequisites

- Docker and Docker Compose
- OpenAI API key (for image generation)

## 🛠️ Tech Stack

### Backend
- Java 25 (OpenJDK 25.0.1)
- Spring Boot 3.2.0
- Spring AI (OpenAI integration)
- PostgreSQL
- Spring WebSocket + STOMP
- Flyway (database migrations)
- Maven

### Frontend
- React 18
- TypeScript
- Vite
- React Router
- STOMP.js + SockJS (WebSocket client)
- Axios

## 🏃 Quick Start

### 1. Clone and Setup

```bash
cd whoisthespy
```

### 2. Configure Environment

Create a `.env` file in the root directory:

```bash
cp .env.example .env
```

Edit `.env` and add your OpenAI API key:

```
OPENAI_API_KEY=sk-your-actual-api-key-here
```

### 3. Build and Run with Docker Compose

```bash
docker-compose up --build
```

This will:
- Start PostgreSQL database
- Build and start the Spring Boot backend
- Build and start the React frontend
- Set up all networking

### 4. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **WebSocket**: ws://localhost:8080/ws

## 📖 Usage

### Creating a Game

1. Go to http://localhost:3000
2. Enter your username
3. Click "Create Game"
4. Share the 6-letter join code with other players

### Joining a Game

1. Enter your username
2. Enter the 6-letter join code
3. Click "Join Game"

### Playing the Game

1. **Lobby**: Wait for all players to join. Host can set the number of spies and start the game.
2. **Game**: 
   - Each player sees their role (spy or civilian) and their word
   - Host can generate AI images related to the civilian word
   - Host can advance turns
   - All players see the same image instantly via WebSocket

## 🏗️ Project Structure

```
whoisthespy/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/whoisthespy/
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── repository/      # Data repositories
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   └── config/          # Configuration
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/    # Flyway migrations
│   │   └── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── pages/                   # React pages
│   │   ├── api/                     # API client
│   │   ├── services/                # WebSocket service
│   │   └── App.tsx
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
└── README.md
```

## 🔌 API Endpoints

### User Management
- `POST /api/users` - Create user
- `GET /api/users/{id}` - Get user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users` - List all users

### Game Management
- `POST /api/game/create` - Create game
- `POST /api/game/join` - Join game
- `POST /api/game/start` - Start game
- `GET /api/game/{gameId}` - Get game
- `POST /api/game/{gameId}/nextTurn` - Advance turn
- `POST /api/game/{gameId}/generateImage` - Generate AI image

### Images
- `GET /api/images/{imageId}` - Get generated image

## 🔌 WebSocket Topics

- `/topic/game/{gameId}/players` - Player join/leave updates
- `/topic/game/{gameId}/image` - AI-generated image updates
- `/topic/game/{gameId}/turn` - Turn changes

## 🧪 Development

### Backend Development

```bash
cd backend
mvn spring-boot:run
```

Make sure PostgreSQL is running (via Docker Compose or locally).

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server will proxy API requests to `http://localhost:8080`.

## 🐳 Docker Commands

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## 🔧 Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      image:
        options:
          model: dall-e-3
          size: 1024x1024
```

### Frontend Configuration

Edit `frontend/vite.config.ts` to change proxy settings or ports.

## 📝 Notes

- Images are stored temporarily in `/tmp/whoisthespy/images` (or configured path)
- The game supports multiple spies
- Civilians get one word, spies get a different related word
- All players see the same AI-generated image instantly

## 🐛 Troubleshooting

### Backend won't start
- Check that PostgreSQL is running and healthy
- Verify your OpenAI API key is set correctly
- Check logs: `docker-compose logs backend`

### Images not generating
- Verify your OpenAI API key is valid
- Check backend logs for errors
- Ensure you have sufficient OpenAI credits

### WebSocket connection fails
- Check that the backend is running on port 8080
- Verify CORS settings in `CorsConfig.java`
- Check browser console for WebSocket errors

## 📄 License

This project is open source and available for educational purposes.

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

