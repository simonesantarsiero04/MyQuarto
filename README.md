Quarto for Android

Description
Quarto is a board game for two players invented by Blaise Müller. The game is played on a 4×4 board with 16 unique pieces. Each piece has four dichotomous attributes – height (tall or short), color (light or dark), shape (square or round), and hollowness (solid or hollow). 
The object of the game is to place the fourth piece in a line of four pieces that share a common attribute. The twist is that your opponent chooses the piece you have to play on your turn.

This project brings the full experience of Quarto to the Android platform, with a clean interface and a robust, well-structured codebase.

Features
Player vs. Player: Full support for a two-player game on a single device.
Configurable Win Conditions: Before starting, players can choose which winning patterns are active (rows, columns, diagonals, and squares of various sizes).
Optional Chess Clock: Players can enable a turn-based timer for an extra challenge. The time per player is configurable.
Complete Game Flow: Implements all rules of Quarto, including piece selection for the opponent, piece placement, and calling "Quarto!".
Special Case Handling: Manages edge cases such as a draw when the board is full or an incorrect "Quarto!" call.
Interactive UI: A clear and responsive user interface that provides feedback on the current game state, active player, and winning moves.

Key Components (Class Breakdown)
The project is composed of several key classes, each with a single, well-defined responsibility.

StartupActivity.java
Role: Configuration Screen. This is the app's entry point. It allows players to configure game settings like the timer and active win conditions.
Function: It gathers user input, validates it, and passes the settings to MainActivity via an Intent. It then removes itself from the back stack.

MainActivity.java
Role: Game Controller. This is the "brain" of the game. It doesn't draw anything on the screen directly but holds the state of the game and directs the flow.
Function: It initializes all logic components (Board, VictoryCheck, ChessClock). It contains the handle... methods that execute game logic in response to user actions (which are forwarded from the GameUIHandler). It listens for events from the ChessClock and GameUIHandler.

GameUIHandler.java
Role: View Manager. This class controls everything the user sees on the game screen.
Function: It finds all View elements, sets up their initial state. It captures user clicks and forwards them to MainActivity via listener callbacks.

Board.java
Role: Data Model. This class represents the state of the game board.
Function: It holds the 4x4 grid, the list of available pieces, and the piece each player is currently holding. It provides methods like assignPieceToPlayer() and placePlayerPiece() that perfectly model the two-phase turn of Quarto.

Piece.java
Role: Data Object. Represents a single, unchangeable game piece.
Function: It uses Java enum types to define the four binary attributes of a piece. Its fields are final, ensuring that a piece's properties cannot be changed after creation.

VictoryCheck.java
Role: Logic Engine. This class encapsulates the complex logic for checking for a win.
Function: It takes the game Board and a set of configuration flags (e.g., checkRows) and determines if a "Quarto" exists. It uses helper methods and Java Generics for clean, non-repetitive code. It returns a detailed VictoryResult object specifying the type and location of the win.

ChessClock.java
Role: Standalone. Manages the optional turn-based timer.
Function: It's completely decoupled from the Quarto game logic. It uses a CountDownTimer and communicates events (time updates and timeouts) outwards via the ChessClockListener interface, making it highly reusable for other projects.

Communication Patterns
The components communicate using the Listener (Callback) Pattern.

UI to Logic: GameUIHandler captures a user's click (e.g., on a board cell). It doesn't know what this click means in terms of game rules. It simply calls a listener method (e.g., onCellClick(row, col)). MainActivity implements this listener and executes the relevant game logic.
Logic to UI: MainActivity decides a change is needed (e.g., a piece is placed). It calls a public method on GameUIHandler (e.g., uiHandler.setPieceOnBoard(row, col, piece)). GameUIHandler then performs the actual UI update.
Component to Logic: ChessClock runs its own timer. When a second passes or a player's time runs out, it calls a method on its ChessClockListener. MainActivity implements this listener to update the UI (via the GameUIHandler) or end the game.
