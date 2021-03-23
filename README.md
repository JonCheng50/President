# President
Multiplayer President Card Game

Created by: Jonathan Cheng
5/20/2020

## How to Play:
Right click Play.command or MultiplayerPres.jar and click run. Follow directions on screen, more instructions can be found at:
https://en.wikipedia.org/wiki/President_(card_game)

Note currently the game is set up with an old IP address, so I have to be the serverGame in order for the game to work.

Additionally, Java is required to play. (https://java.com/en/download/)

Thank you for playing!



## How the Game Works:
Each game of President consists of a ServerGame and anywhere from 0 to 3 ClientGames. The ServerGame communicates
with all of the ClientGames, starting with a StartPacket that basically contains the information on who has what
cards (so that everyone is actually playing the same game). Whenever someone plays a card or passes (on their turn),
and UpdatePacket is sent between all the games. Any CPU activity is done from the ServerGame, which updates all of the
ClientGames accordingly.

All of the game logic, like what you can play and when, is handled within each game individually. Because all of the
games are identical at all times (through UpdatePackets), this ensures accurate multiplayer play while not
unnecessarily sending excess information between games.

If there are any problems with the game, please contact joncheng@seas.upenn.edu