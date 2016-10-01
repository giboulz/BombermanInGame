import java.util.*;



import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/
class Player {

	/*TODO : 
	 * faire les item
	 * les rammasser
	 * rester hors range des explosions.
	 * 
	 * 
	 */
	
	
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		loadConfiguration(in);

		// game loop
		while (true) {
			Grid grid = new Grid();
			Entities entities = new Entities();

			loadGrid(in, grid);

			loadEntity(in, entities);

			for(Bomb b: entities.bombs){
				System.err.println(b);
			}
			
			
			Intention intent = getBasicMouvementMoveAndWait(grid, entities);

			System.out.println(intent);
		}
	}

	private static Intention getBasicMouvementMoveAndWait(Grid grid, Entities entities) {
		Intention intent = new Intention();
		// Intention intent = new Intention(Mouvement.BOMB, 6, 5);
		Joueur myPlayer = entities.getMyPlayer();

		grid.calculateAccessibleTuileForPlayer(myPlayer);

		grid.resolvePendingExplosition(entities.bombs);

		int res[][] = grid.getNumberHitBoxForBomb(myPlayer.explodingRange);
		/*
		 * for (int j = 0; j < Configuration.height; j++) { for (int i = 0; i <
		 * Configuration.width; i++) { System.err.print(res[i][j]); }
		 * System.err.println(""); }
		 */

		grid.reduceBombingSpotToWalkablePath(myPlayer.owner, res);

		Position pos = getBestBombingSpot(myPlayer, res);

		if (myPlayer.nbLeftBomb >= 1 && myPlayer.pos.equals(pos)) {
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
			loadAnEntity(in, entities);
		}
		in.nextLine();
	}

	private static void loadAnEntity(Scanner in, Entities entities) {
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
		case Configuration.ENTITY_ITEM : 
			if(param1 == Configuration.ITEM_RANGE){
				e = new ItemRange(x,y); 
			}else if(param1 == Configuration.ITEM_BOMB){
				e = new ItemBomb(x,y); 
			}
		}
		entities.addEntity(e);
	}

	private static void loadGrid(Scanner in, Grid grid) {
		for (int y = 0; y < Configuration.height; y++) {
			String row = in.nextLine();
			String[] tab = row.split("");

			for (int i = 0; i < tab.length; i++) {
				if (tab[i].compareTo(".") == 0) {
					grid.grid[i][y] = new Sol();
				} else if(tab[i].compareTo("X") == 0){
					grid.grid[i][y] = new Wall();
				}else{
					grid.grid[i][y] = new Caisse();
				}

			}
		}
	}

	private static void loadConfiguration(Scanner in) {
		Configuration.width = in.nextInt();
		Configuration.height = in.nextInt();
		Configuration.myId = in.nextInt();
		System.err.println("height :"+ Configuration.height);
		System.err.println("width :"+ Configuration.width);
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
	public static final int ITEM_BOMB = 1;
	public static final int ITEM_RANGE = 2;
	
	public static final int ENTITY_JOUEUR = 0;
	public static final int ENTITY_BOMB = 1;
	public static final int ENTITY_ITEM = 2;

	public static int myId;
	public static int height;
	public static int width;
}

class Entities {
	public List<Entity> list;
	public List<Joueur> joueurs;
	public List<Bomb> bombs;
	public List<Item> items;
	

	public Entities() {
		list = new ArrayList<Entity>();
		joueurs = new ArrayList<Joueur>();
		bombs = new ArrayList<Bomb>();
		items = new ArrayList<Item>();
	}
	
	
	

	public void addEntity(Entity e) {
		list.add(e);
		if (e instanceof Joueur) {
			joueurs.add((Joueur) e);
		}
		if (e instanceof Bomb) {
			bombs.add((Bomb) e);
		}
		if(e instanceof Item){
			items.add((Item) e); 
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
	
	public String toString(){
		return "Joueur : "+owner+" "+pos; 
	}
}

class Bomb extends Entity {
	public int leftRoundToExplode;
	public int explodingRange;
	public boolean haveExplode;

	public Bomb(int x, int y, int owner, int leftRoundToExplode, int explodingRange) {
		super(x, y, owner);
		this.leftRoundToExplode = leftRoundToExplode;
		this.explodingRange = explodingRange;
		this.haveExplode = false; 
	}
	
	public String toString(){
		return (pos + " expl: "+explodingRange+" timer : "+leftRoundToExplode); 
	}
}

class Item extends Entity{

	public Item(int x, int y) {
		super(x, y, 0);
	}
	
}

class ItemRange extends Item{

	public ItemRange(int x, int y) {
		super(x, y);
	}
	
}

class ItemBomb extends Item{

	public ItemBomb(int x, int y) {
		super(x, y);
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
	
	public String toString(){
		return "("+x+","+y+")"; 
	}

}

class Grid {
	public Tuile[][] grid;

	public Map<Integer, boolean[][]> playerMobility;

	public Grid() {
		grid = new Tuile[Configuration.width][Configuration.height];

		for (int i = 0; i < Configuration.width; i++) {
			for (int y = 0; y < Configuration.height; y++) {
				grid[i][y] = new Tuile();
			}
		}

		playerMobility = new HashMap<Integer, boolean[][]>();

	}
	
	public Grid(Grid g){
		playerMobility = new HashMap<Integer, boolean[][]>();
		for (int i = 0; i < Configuration.width; i++) {
			for (int y = 0; y < Configuration.height; y++) {
				Tuile t =g.grid[i][y];  
				if(t instanceof Sol){
					grid[i][y] = new Sol(); 
				}
				if(t instanceof Caisse){
					grid[i][y] = new Caisse(); 
				}
				if(t instanceof Wall){
					grid[i][y] = new Wall(); 
				}
				 
			}
		}
		
	}

	public void reduceBombingSpotToWalkablePath(int playerId, int[][] res) {
		boolean[][] walkablePathForPlayer = playerMobility.get(playerId);
		if (walkablePathForPlayer != null) {
			for (int i = 0; i < Configuration.width; i++) {
				for (int y = 0; y < Configuration.height; y++) {
					if (walkablePathForPlayer[i][y] == false) {
						res[i][y] = 0;
					}
				}
			}
		}

	}

	public void calculateAccessibleTuileForPlayer(Joueur myPlayer) {
		boolean[][] res = new boolean[Configuration.width][Configuration.height];
		for (int i = 0; i < Configuration.width; i++) {
			for (int y = 0; y < Configuration.height; y++) {
				res[i][y] = false;
			}
		}
		calculateMovingPath(myPlayer.pos, res);

		playerMobility.put(myPlayer.owner, res);

	}

	private void calculateMovingPath(Position pos, boolean[][] res) {
		if (!outOfBound(pos)) {
			int x = pos.x;
			int y = pos.y;
			if (grid[x][y].isWalkable() && res[x][y] == false) {
				res[x][y] = true;

				calculateMovingPath(new Position(x + 1, y), res);
				calculateMovingPath(new Position(x - 1, y), res);
				calculateMovingPath(new Position(x, y + 1), res);
				calculateMovingPath(new Position(x, y + 1), res);
			}
		}

	}

	public void resolvePendingExplosition(List<Bomb> bombs) {
		for (Bomb b : bombs) {
			resolveBomb(b, 0, 1);
			resolveBomb(b, 0, -1);
			resolveBomb(b, 1, 0);
			resolveBomb(b, -1, 0);
		}

	}

	private void resolveBomb(Bomb b, int xOffset, int yOffset) {
		boolean res = false;
		int i = b.pos.x;
		int j = b.pos.y;
		for (int z = 0; z < b.explodingRange; z++) {
			if (!outOfBound(i, j) && !res) {
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
		res.value = 0;
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

	public boolean hasBomb;
	public boolean hasItem;
	public boolean box;
	public boolean walkable;
	public boolean stopDeflagration; 

	public Tuile() {
		hasItem = false; 
		stopDeflagration = false; 
	}

	public boolean isBox() {
		return false;
	}

	public boolean isWalkable() {
		return false;
	}
	
	public boolean hasItem(){
		return hasItem; 
	}
	
	public boolean isStopDeflagration(){
		return stopDeflagration; 
	}
}

class Caisse extends Tuile {
	public Caisse(){
		super(); 
		this.stopDeflagration = true; 
	}
	
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

	public boolean isWalkable() {
		return true;
	}
}

class Wall extends Tuile {
	
	public Wall(){
		super(); 
		this.stopDeflagration = true; 
	}
	
	public String toString() {
		return "X";
	}

	public boolean isBox() {
		return false;
	}
	
	public boolean isWalkable() {
		return false;
	}
}