import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/
class Player {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		loadConfiguration(in);

		// game loop
		while (true) {
			Grid grid = new Grid();
			Entities entities = new Entities();

			loadGrid(in, grid);

			loadEntity(in, entities);

			Intention intent = getBasicMouvementMoveAndWait(grid, entities);

			System.out.println(intent);
		}
	}

	private static Intention getBasicMouvementMoveAndWait(Grid grid, Entities entities) {
		Intention intent = new Intention();
		// Intention intent = new Intention(Mouvement.BOMB, 6, 5);
		Joueur myPlayer = entities.getMyPlayer();

		grid.resolvePendingExplosition(entities.bombs);

		int res[][] = grid.getNumberHitBoxForBomb(myPlayer.explodingRange);
		/*
		 * for (int j = 0; j < Configuration.height; j++) { for (int i = 0; i <
		 * Configuration.width; i++) { System.err.print(res[i][j]); }
		 * System.err.println(""); }
		 */

		Position pos = getBestBombingSpot(myPlayer, res);

		if (myPlayer.nbLeftBomb == 1 && myPlayer.pos.equals(pos)) {
			intent.move = Mouvement.BOMB;
		} else {
			intent.move = Mouvement.MOVE;
		}

		intent.x = pos.x;
		intent.y = pos.y;

		return intent;
	}

	private static Position getBestBombingSpot(Joueur myPlayer, int[][] res) {
		Position pos = myPlayer.pos;
		pos.value = 0;
		int i = 0;
		while (pos.value == 0 && i < 5) {
			i++;
			pos = Grid.getBestPositionWithinGivenMouvements(res, i * 5, myPlayer.pos);
		}
		return pos;
	}

	private static void loadEntity(Scanner in, Entities entities) {
		int entitiesIn = in.nextInt();
		for (int i = 0; i < entitiesIn; i++) {
			int entityType = in.nextInt();
			int owner = in.nextInt();
			int x = in.nextInt();
			int y = in.nextInt();
			int param1 = in.nextInt();
			int param2 = in.nextInt();

			Entity e = null;

			switch (entityType) {
			case Configuration.ENTITY_BOMB:
				e = new Bomb(x, y, owner, param1, param2);
				break;
			case Configuration.ENTITY_JOUEUR:
				e = new Joueur(x, y, owner, param1, param2);
				break;
			}

			entities.addEntity(e);

		}
		in.nextLine();
	}

	private static void loadGrid(Scanner in, Grid grid) {
		for (int y = 0; y < Configuration.height; y++) {
			String row = in.nextLine();
			String[] tab = row.split("");

			for (int i = 0; i < tab.length; i++) {
				if (tab[i].compareTo(".") == 0) {
					grid.grid[i][y] = new Sol();
				} else {
					grid.grid[i][y] = new Caisse();
				}

			}
		}
	}

	private static void loadConfiguration(Scanner in) {
		Configuration.width = in.nextInt();
		Configuration.height = in.nextInt();
		Configuration.myId = in.nextInt();
		in.nextLine();
	}
}

enum Mouvement {
	MOVE("MOVE"), BOMB("BOMB");

	private String name = "";

	Mouvement(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}

class Intention {
	public Mouvement move;
	public int x;
	public int y;

	public Intention() {

	}

	public Intention(Mouvement move, int x, int y) {
		this.move = move;
		this.x = x;
		this.y = y;
	}

	public String toString() {
		String s = move.toString() + " " + x + " " + y;
		return s;
	}

}

class Configuration {
	public static final int ENTITY_JOUEUR = 0;
	public static final int ENTITY_BOMB = 1;

	public static int myId;
	public static int height;
	public static int width;
}

class Entities {
	List<Entity> list;
	List<Joueur> joueurs;
	List<Bomb> bombs;

	public Entities() {
		list = new ArrayList<Entity>();
		joueurs = new ArrayList<Joueur>();
		bombs = new ArrayList<Bomb>();
	}

	public void addEntity(Entity e) {
		list.add(e);
		if (e instanceof Joueur) {
			joueurs.add((Joueur) e);
		}
		if (e instanceof Bomb) {
			bombs.add((Bomb) e);
		}

	}

	public Joueur getMyPlayer() {
		Joueur me = null;
		for (Joueur joueur : joueurs) {
			if (joueur.owner == Configuration.myId) {
				me = joueur;
			}
		}
		return me;
	}

}

class Entity {
	public Position pos;
	public int owner;

	public Entity(int x, int y, int owner) {
		pos = new Position(x, y);
		this.owner = owner;
	}

}

class Joueur extends Entity {
	public int nbLeftBomb;
	public int explodingRange;

	public Joueur(int x, int y, int owner, int nbLeftBomb, int explodingRange) {
		super(x, y, owner);
		this.nbLeftBomb = nbLeftBomb;
		this.explodingRange = explodingRange;
	}
}

class Bomb extends Entity {
	public int leftRoundToExplode;
	public int explodingRange;

	public Bomb(int x, int y, int owner, int leftRoundToExplode, int explodingRange) {
		super(x, y, owner);
		this.leftRoundToExplode = leftRoundToExplode;
		this.explodingRange = explodingRange;
	}
}

class Position {
	public int x;
	public int y;
	public int value;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Position other = (Position) obj;
		return x == other.x && y == other.y;
	}

}

class Grid {
	public Tuile[][] grid;

	public Grid() {
		grid = new Tuile[Configuration.width][Configuration.height];

		for (int i = 0; i < Configuration.width; i++) {
			for (int y = 0; y < Configuration.height; y++) {
				grid[i][y] = new Tuile();
			}
		}
	}

	public void resolvePendingExplosition(List<Bomb> bombs) {
		for (Bomb b : bombs) {
			resolveBomb(b, 0,1);
			resolveBomb(b, 0,-1);
			resolveBomb(b, 1,0);
			resolveBomb(b, -1,0);
		}

	}

	private void resolveBomb(Bomb b, int xOffset, int yOffset) {
		boolean res = false;
		int i = b.pos.x; 
		int j = b.pos.y; 
		for (int z = 0; z < b.explodingRange; z++) {
			if (!outOfBound(i,j) && !res) {
				if (grid[i][j].isBox()) {
					res = true;
					grid[i][j] = new Sol(); 
				}
				i += xOffset;
				j += yOffset;

			}

		}
	}

	public static Position getBestPositionWithinGivenMouvements(int[][] tabOfBombPlacement, int nbMouvement,
			Position actual) {
		Position res = new Position(0, 0);
		res.value = 0 ; 
		int nbBox = 0;

		for (int i = actual.x - nbMouvement; i < actual.x + nbMouvement; i++) {
			for (int j = actual.y - nbMouvement; j < actual.y + nbMouvement; j++) {
				if (!outOfBound(i, j)) {
					if (tabOfBombPlacement[i][j] > nbBox) {
						res = new Position(i, j);

						nbBox = tabOfBombPlacement[i][j];
						res.value = nbBox;
					}
				}
			}
		}
		return res;
	}

	public int[][] getNumberHitBoxForBomb(int explodingRange) {
		int[][] res = new int[Configuration.width][Configuration.height];

		for (int i = 0; i < Configuration.width; i++) {
			for (int j = 0; j < Configuration.height; j++) {
				int boxUp = 0;
				int boxRight = 0;
				int boxDown = 0;
				int boxLeft = 0;
				if (!isBoxIsOnTuile(i, j)) {
					boxUp = calculateIfBoxIsInExplodingRange(i, j, explodingRange, 0, -1);
					boxRight = calculateIfBoxIsInExplodingRange(i, j, explodingRange, 1, 0);
					boxDown = calculateIfBoxIsInExplodingRange(i, j, explodingRange, 0, 1);
					boxLeft = calculateIfBoxIsInExplodingRange(i, j, explodingRange, -1, 0);
				}
				res[i][j] = boxUp + boxRight + boxDown + boxLeft;

			}
		}

		return res;
	}

	private int calculateIfBoxIsInExplodingRange(int i, int j, int explodingRange, int xOffset, int yOffset) {
		boolean res = false;
		for (int z = 0; z < explodingRange; z++) {
			if (!outOfBound(i, j)) {
				if (grid[i][j].isBox()) {
					res = true;
				}
				i += xOffset;
				j += yOffset;

			}

		}
		if (res)
			return 1;
		else
			return 0;

	}

	private static boolean outOfBound(Position pos) {
		return outOfBound(pos.x, pos.y);
	}

	private static boolean outOfBound(int i, int j) {

		if (i < 0 || j < 0 || i > Configuration.width - 1 || j > Configuration.height - 1) {
			return true;
		}
		return false;
	}

	public boolean isBoxIsOnTuile(int x, int y) {
		return grid[x][y].isBox();
	}

	public String toString() {
		String s = "";

		for (int y = 0; y < Configuration.height; y++) {
			for (int i = 0; i < Configuration.width; i++) {
				s += grid[i][y].toString();
			}
			s += "\n";
		}
		return s;
	}
}

class Tuile {

	public boolean box;

	public Tuile() {

	}

	public boolean isBox() {
		return false;
	}

}

class Caisse extends Tuile {
	public String toString() {
		return "0";
	}

	public boolean isBox() {
		return true;
	}
}

class Sol extends Tuile {
	public String toString() {
		return ".";
	}
}