// "static void main" must be defined in a public class.]

/*
TODO:
- game state: won, lost, in progress
- check if three in a row are all cats, then game won
- check eight items on board to see what they are and if won
- refactor getadjacentneighbor function
- parameterize items in inventory and board size so it can scale
*/

import java.util.*;

// Store the coordinates of the pieces
class Board {
    private char[][] board;
    private int size;

    final public static char BLANK = '_';
    final public static char CAT = 'C';
    final public static char KITTEN = 'K';

    public char get(int x, int y) {
        return board[x][y];
    }

    public void set(int x, int y, char ch) {
        board[x][y] = ch;
    }

    public Board(int size) {
        this.size = size;

        board = new char[size][size];

        for (int i = 0; i < size; i++) {
            Arrays.fill(board[i], BLANK);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append("|");
            for (int j = 0; j < size; j++) {
                sb.append(board[i][j]);
                sb.append("|");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public int getSize() {
        return size;
    }

    public boolean inBounds(int x, int y) {
        int n = this.getSize();

        return (x >= 0 && y >= 0 && x < n && y < n);
    }

    public static String pieceType(char ch) {
        if (ch == 'k' || ch == 'K') return "Kitten";
        if (ch == 'c' || ch == 'C') return "Cat";
        return "";
    }

    public List<int[]> getNeighbors(int x, int y) {
        int n = this.getSize();

        List<int[]> neighbors = new ArrayList<>();

        if (x > 0) neighbors.add(new int[]{x-1, y});
        if (y > 0) neighbors.add(new int[]{x, y-1});
        if (x > 0 && y > 0) neighbors.add(new int[]{x-1, y-1});
        if (x > 0 && y < n-1) neighbors.add(new int[]{x-1, y+1});
        if (x < n-1 && y > 0) neighbors.add(new int[]{x+1, y-1});
        if (x < n-1 && y < n-1) neighbors.add(new int[]{x+1, y+1});
        if (x < n-1) neighbors.add(new int[]{x+1, y});
        if (y < n-1) neighbors.add(new int[]{x, y+1});

        return neighbors;
    }

    public char neighborAdjacentType(int x, int y, int neighborX, int neighborY) {
        int n = this.getSize();

        int cellToCheckX = neighborX + (neighborX - x);
        int cellToCheckY = neighborY + (neighborY - y);

        if (inBounds(cellToCheckX, cellToCheckY)) return this.get(cellToCheckX, cellToCheckY);
        return Board.BLANK;
    }
}

// Contain the pieces and able to place pieces
class Player {
    private Board board;
    private Boop game;
    private int kittenCount = 8;
    private int catCount = 0;
    private char kittenCh;
    private char catCh;

    public Player(Board board, Boop game, char kittenCh, char catCh) {
        this.board = board;
        this.game = game;
        this.kittenCh = kittenCh;
        this.catCh = catCh;
    }

    public String getStatus() {
        return "Kitten count: " + kittenCount + " | Cat count: " + catCount;
    }

    public char getKittenCh() {
        return this.kittenCh;
    }

    public char getCatCh() {
        return this.catCh;
    }

    public void returnKitten() {
        this.kittenCount++;
    }

    public void returnCat() {
        this.catCount++;
    }

    public void place(String piece, int x, int y) {
        // Checks valid piece type
        if (piece != "Kitten" && piece != "Cat") {
            System.out.println("Invalid piece");
            return;
        }

        // Checks spot not occupied
        if (board.get(x, y) != Board.BLANK) {
            System.out.println("Spot occupied!");
            return;
        }

        if (!board.inBounds(x, y)) {
            System.out.println("X and Y are out of bounds");
            return;
        }

        switch(piece) {
            case "Kitten":
                if (kittenCount <= 0) {
                    System.out.println("No kittens");
                    return;
                }
                this.kittenCount--;
                board.set(x, y, kittenCh);
                break;
            case "Cat":
                if (catCount <= 0) {
                    System.out.println("No cats");
                    return;
                }
                this.catCount--;
                board.set(x, y, catCh);
                break;
            default:
                break;
        }

        // Handle booping
        for (int[] neighbor : board.getNeighbors(x, y)) {
            int neighborX = neighbor[0];
            int neighborY = neighbor[1];

            // Neighbor is blank, nothing to do
            if (board.get(neighborX, neighborY) == Board.BLANK) continue;
            // There is a neighbor, but it can't be booped
            if (board.neighborAdjacentType(x, y, neighborX, neighborY) != Board.BLANK) continue;

            // It passed checks and can be booped
            boopNeighbor(x, y, neighborX, neighborY, piece);
        }

        game.postTurnChecks();

        return;
    }

    private void boopNeighbor(int x, int y, int neighborX, int neighborY, String piece) {
        int n = board.getSize();

        int changeX = neighborX - x;
        int changeY = neighborY - y;

        int newX = neighborX + changeX;
        int newY = neighborY + changeY;

        char neighborPiece = board.get(neighborX, neighborY);
        board.set(neighborX, neighborY, Board.BLANK);

        // Kitten cannot boop cat
        if (piece.equals("Kitten")
                && Board.pieceType(neighborPiece).equals("Cat")) {
            return;
        }

        // Update if on board
        if (board.inBounds(newX, newY)) {
            board.set(newX, newY, neighborPiece);
            return;
        }

        // Update if off board
        game.returnPiece(neighborPiece);
    }
}

// Run the game
// Run the logic after each turn and check win/loss
class Boop {
    Board board;
    Player playerOne;
    Player playerTwo;

    public Boop(Board board) {
        this.board = board;
        System.out.println(board.toString());
        System.out.println("Game started!");
        System.out.println("");
    }

    public void setPlayerOne(Player playerOne) {
        this.playerOne = playerOne;
    }

    public void setPlayerTwo(Player playerTwo) {
        this.playerTwo = playerTwo;
    }

    // Piece booped off, return to player
    public void returnPiece(char piece) {
        if (piece == playerOne.getKittenCh()) playerOne.returnKitten();
        if (piece == playerTwo.getKittenCh()) playerTwo.returnKitten();
        if (piece == playerOne.getCatCh()) playerOne.returnCat();
        if (piece == playerTwo.getCatCh()) playerTwo.returnCat();
    }

    // Piece booped off, return to player
    public void returnPieceThreeInARow(char piece) {
        if (piece == playerOne.getKittenCh() || piece == playerOne.getCatCh()) playerOne.returnCat();
        if (piece == playerTwo.getKittenCh() || piece == playerTwo.getCatCh()) playerTwo.returnCat();
    }

    public void postTurnChecks() {
        threeInARow();
        eightOnBoard();

        System.out.println(board.toString());
        System.out.println("Player one: " + playerOne.getStatus());
        System.out.println("Player two: " + playerTwo.getStatus());
        System.out.println("");
    }

    // Check if three pieces in a row for a player
    // If it's three cats, they win
    // If it's not three cats, remove kittens from board and swap kittens for cats in hand
    public void threeInARow() {
        int n = board.getSize();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                char piece = board.get(i, j);
                if (piece == Board.BLANK) continue;

                for (int[] neighbor : board.getNeighbors(i, j)) {
                    int neighborX = neighbor[0];
                    int neighborY = neighbor[1];

                    int neighborAdjX = neighborX + (neighborX - i);
                    int neighborAdjY = neighborY + (neighborY - j);

                    char neighborPiece = board.get(neighborX, neighborY);
                    char neighborAdjPiece = board.inBounds(neighborAdjX, neighborAdjY) ? board.get(neighborAdjX, neighborAdjY) : Board.BLANK;

                    // Three in a row
                    if (piece == neighborPiece && piece == neighborAdjPiece) {
                        returnPieceThreeInARow(board.get(i, j));
                        returnPieceThreeInARow(board.get(neighborX, neighborY));
                        returnPieceThreeInARow(board.get(neighborAdjX, neighborAdjY));

                        board.set(i, j, Board.BLANK);
                        board.set(neighborX, neighborY, Board.BLANK);
                        board.set(neighborAdjX, neighborAdjY, Board.BLANK);

                        /*
                        System.out.println("Three in a row");

                        System.out.println("Piece: " + piece + "(" + i + "," + j + ")");
                    System.out.println("Neighbor piece: " + neighborPiece + "(" + neighborX + "," + neighborY + ")");
                    System.out.println("Neighbor adj piece: " + neighborAdjPiece + "(" + neighborAdjX + "," + neighborAdjY + ")");
                    */
                    }
                }
            }
        }
    }

    // Check if all 8 kittens on board
    // Return one kitten to player of their choice and exchange for cat in hand
    // Check if all 8 cats on board (player won)
    public void eightOnBoard() {
        // Check if all cats; if so, won

        // Otherwise, exchange one kitten for cat and return to hand

        return;
    }
}

// Run an instance of the game
public class Main {
    public static void main(String[] args) {
        Board board = new Board(6);
        Boop game = new Boop(board);
        Player playerOne = new Player(board, game, 'K', 'C');
        Player playerTwo = new Player(board, game, 'k', 'c');
        game.setPlayerOne(playerOne);
        game.setPlayerTwo(playerTwo);

        /*
        playerOne.place("Kitten", 2, 2);
        playerTwo.place("Kitten", 3, 3);
        playerOne.place("Kitten", 1, 2);
        playerTwo.place("Kitten", 1, 1);
        */
        playerOne.place("Kitten", 0, 0);
        playerOne.place("Kitten", 1, 1);
        playerOne.place("Kitten", 2, 2);
        playerOne.place("Kitten", 3, 3);
        playerOne.place("Kitten", 2, 2);

    }
}