import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { userApi, gameApi } from '../api/client'
import '../App.css'

function JoinGame() {
  const [username, setUsername] = useState('')
  const [joinCode, setJoinCode] = useState('')
  const [isCreating, setIsCreating] = useState(false)
  const [isJoining, setIsJoining] = useState(false)
  const [error, setError] = useState('')
  const [createdGameId, setCreatedGameId] = useState<string | null>(null)
  const navigate = useNavigate()

  const handleCreateGame = async () => {
    if (!username.trim()) {
      setError('Please enter a username')
      return
    }

    setIsCreating(true)
    setError('')

    try {
      const user = await userApi.create(username)
      const game = await gameApi.create(user.id)
      setCreatedGameId(game.id)
      // Navigate to lobby after a short delay
      setTimeout(() => {
        navigate(`/lobby/${game.id}`, { state: { userId: user.id, isHost: true } })
      }, 2000)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create game')
      setIsCreating(false)
    }
  }

  const handleJoinGame = async () => {
    if (!username.trim()) {
      setError('Please enter a username')
      return
    }
    if (!joinCode.trim() || joinCode.length !== 6) {
      setError('Please enter a valid 6-letter join code')
      return
    }

    setIsJoining(true)
    setError('')

    try {
      const user = await userApi.create(username)
      const game = await gameApi.join(joinCode.toUpperCase(), user.id)
      navigate(`/lobby/${game.id}`, { state: { userId: user.id, isHost: false } })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to join game')
      setIsJoining(false)
    }
  }

  return (
    <div className="container">
      <div className="card">
        <h1>🎭 Who Is The Spy</h1>
        
        {createdGameId ? (
          <div className="success">
            <h2>Game Created!</h2>
            <p>Redirecting to lobby...</p>
          </div>
        ) : (
          <>
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                disabled={isCreating || isJoining}
              />
            </div>

            <button
              className="btn btn-primary"
              onClick={handleCreateGame}
              disabled={isCreating || isJoining}
            >
              {isCreating ? 'Creating Game...' : 'Create Game'}
            </button>

            <div style={{ textAlign: 'center', margin: '1.5rem 0', color: '#666' }}>
              OR
            </div>

            <div className="form-group">
              <label htmlFor="joinCode">Join Code</label>
              <input
                id="joinCode"
                type="text"
                value={joinCode}
                onChange={(e) => setJoinCode(e.target.value.toUpperCase().slice(0, 6))}
                placeholder="Enter 6-letter code"
                maxLength={6}
                disabled={isCreating || isJoining}
                style={{ textTransform: 'uppercase', letterSpacing: '0.5rem' }}
              />
            </div>

            <button
              className="btn btn-secondary"
              onClick={handleJoinGame}
              disabled={isCreating || isJoining}
            >
              {isJoining ? 'Joining...' : 'Join Game'}
            </button>

            {error && <div className="error">{error}</div>}
          </>
        )}
      </div>
    </div>
  )
}

export default JoinGame

