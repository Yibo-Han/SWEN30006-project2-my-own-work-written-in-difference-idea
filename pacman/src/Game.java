// PacMan.java
// Simple PacMan implementation
package src;

import ch.aplu.jgamegrid.*;
import src.utility.GameCallback;

import java.awt.*;
import java.util.*;

public class Game extends GameGrid
{
  private final static int nbHorzCells = 20;
  private final static int nbVertCells = 11;
  protected PacManGameGrid grid = new PacManGameGrid(nbHorzCells, nbVertCells);

  protected PacActor pacActor = new PacActor(this);
  private Monster troll = new Monster(this, MonsterType.Troll);
  private Monster tx5 = new Monster(this, MonsterType.TX5);


  private ArrayList<Location> pillAndItemLocations = new ArrayList<Location>();
  private ArrayList<Actor> iceCubes = new ArrayList<Actor>();
  private ArrayList<Actor> goldPieces = new ArrayList<Actor>();
  private GameCallback gameCallback;
  private Properties properties;
  private int seed = 30006;
  private ArrayList<Location> propertyPillLocations = new ArrayList<>();
  private ArrayList<Location> propertyGoldLocations = new ArrayList<>();

  private Map<String, String> WhiteTiles = new HashMap<>();
  private Map<String, String> YellowTiles = new HashMap<>();
  private Map<String, String> DarkGoldTiles = new HashMap<>();
  private Map<String, String> DarkGrayTiles = new HashMap<>();

  public Game(GameCallback gameCallback, Properties properties,StringBuilder stringBuilder)
  {
    //Setup game
    super(nbHorzCells, nbVertCells, 32, false);
    this.gameCallback = gameCallback;
    this.properties = properties;
    String tx5Location = properties.getProperty("TX5.location");
    String trollLocation = properties.getProperty("Troll.location");
    if (stringBuilder != null) {
      grid.setStringBuilder(stringBuilder);
      grid.setMazeArray(stringBuilder);
    }
    setSimulationPeriod(100);
    setTitle("[PacMan in the Multiverse]");

// 解析Portal参数
    String portalWhiteListStr = this.properties.getProperty("PortalWhiteTile.location");
    if (portalWhiteListStr != null && !portalWhiteListStr.isEmpty()) {
      String[] PortalWhiteList = portalWhiteListStr.split(";");
      if (PortalWhiteList.length > 1) {
        this.WhiteTiles.put(PortalWhiteList[0], PortalWhiteList[1]);
        this.WhiteTiles.put(PortalWhiteList[1], PortalWhiteList[0]);
      }
    }

    String portalYellowTileListStr = this.properties.getProperty("PortalYellowTile.location");
    if (portalYellowTileListStr != null && !portalYellowTileListStr.isEmpty()) {
      String[] PortalYellowTileList = portalYellowTileListStr.split(";");
      if (PortalYellowTileList.length > 1) {
        this.YellowTiles.put(PortalYellowTileList[0], PortalYellowTileList[1]);
        this.YellowTiles.put(PortalYellowTileList[1], PortalYellowTileList[0]);
      }
    }

    String portalDarkGoldTileListStr = this.properties.getProperty("PortalDarkGoldTile.location");
    if (portalDarkGoldTileListStr != null && !portalDarkGoldTileListStr.isEmpty()) {
      String[] PortalDarkGoldTileList = portalDarkGoldTileListStr.split(";");
      if (PortalDarkGoldTileList.length > 1) {
        this.DarkGoldTiles.put(PortalDarkGoldTileList[0], PortalDarkGoldTileList[1]);
        this.DarkGoldTiles.put(PortalDarkGoldTileList[1], PortalDarkGoldTileList[0]);
      }
    }

    String portalDarkGrayTileListStr = this.properties.getProperty("PortalDarkGrayTile.location");
    if (portalDarkGrayTileListStr != null && !portalDarkGrayTileListStr.isEmpty()) {
      String[] PortalDarkGrayTileList = portalDarkGrayTileListStr.split(";");
      if (PortalDarkGrayTileList.length > 1) {
        this.DarkGrayTiles.put(PortalDarkGrayTileList[0], PortalDarkGrayTileList[1]);
        this.DarkGrayTiles.put(PortalDarkGrayTileList[1], PortalDarkGrayTileList[0]);
      }
    }



    //Setup for auto test
    pacActor.setPropertyMoves(properties.getProperty("PacMan.move"));
    pacActor.setAuto(Boolean.parseBoolean(properties.getProperty("PacMan.isAuto")));
    loadPillAndItemsLocations();
    GGBackground bg = getBg();
    drawGrid(bg);

    //Setup Random seeds
    seed = Integer.parseInt(properties.getProperty("seed"));
    pacActor.setSeed(seed);
    troll.setSeed(seed);
    tx5.setSeed(seed);
    addKeyRepeatListener(pacActor);
    setKeyRepeatPeriod(150);
    troll.setSlowDown(3);
    tx5.setSlowDown(3);
    pacActor.setSlowDown(3);
    tx5.stopMoving(5);
    setupActorLocations();


    //Run the game
    doRun();
    show();

// 创建一个新的线程来执行游戏逻辑
    Thread gameThread = new Thread(() -> {
      setupPillAndItemsLocations();
      int maxPillsAndItems = countPillsAndItems();
      boolean hasPacmanBeenHit = false;
      boolean hasPacmanEatAllPills = false;
      do {
        if(!tx5Location.isEmpty()){
          hasPacmanBeenHit = tx5.getLocation().equals(pacActor.getLocation());
          if (hasPacmanBeenHit) break;
        }
        if(!trollLocation.isEmpty()){
          hasPacmanBeenHit = troll.getLocation().equals(pacActor.getLocation());
          if (hasPacmanBeenHit) break;
        }
        hasPacmanEatAllPills = pacActor.getNbPills() >= maxPillsAndItems;
        delay(10);
        if (hasPacmanEatAllPills) break;
      } while (true);

      delay(120);
      Location loc = pacActor.getLocation();
      troll.setStopMoving(true);
      tx5.setStopMoving(true);
      pacActor.removeSelf();
      String title = "";
      if (hasPacmanBeenHit) {
        bg.setPaintColor(Color.red);
        title = "GAME OVER";
        addActor(new Actor("sprites/explosion3.gif"), loc);
      } else if (hasPacmanEatAllPills) {
        bg.setPaintColor(Color.yellow);
        title = "YOU WIN";
      }
      setTitle(title);
      gameCallback.endOfGame(title);
      doPause();
    });

// 启动游戏线程
    gameThread.start();


  }

  public Location search_local(Location location) {
    String key = location.getX() + "," + location.getY();
    String value = "";
    if(WhiteTiles.containsKey(key)) {
      value = WhiteTiles.get(key);
    } else if(YellowTiles.containsKey(key)) {
      value = YellowTiles.get(key);
    } else if(DarkGrayTiles.containsKey(key)) {
      value = DarkGrayTiles.get(key);
    } else if(DarkGoldTiles.containsKey(key)) {
      value = DarkGoldTiles.get(key);
    }
    if(value.equals("")) {
      return null;
    }
    String loc[] = value.split(",");
    return new Location(Integer.parseInt(loc[0]), Integer.parseInt(loc[1]));
  }
  private void handleInfo(StringBuilder stringBuilder) {
    // generate map info
  }

  public GameCallback getGameCallback() {
    return gameCallback;
  }

  // get location from properties/xml for pac man and monster
  private void setupActorLocations() {
//    for (int i = 0; i < maze.length; i++) {
//      for (int j = 0; j < maze[0].length; j++) {
//        if (maze[i][j] == 6) {
//          addActor(troll, new Location(i, j), Location.NORTH);
//        } else if (maze[i][j] == 7) {
//          addActor(tx5, new Location(i, j), Location.NORTH);
//        } else if (maze[i][j] == 5) {
//          addActor(pacActor, new Location(i, j));
//        }
//      }
//    }
    String[] trollLocations = this.properties.getProperty("Troll.location").split(",");
    String[] tx5Locations = this.properties.getProperty("TX5.location").split(",");
    String[] pacManLocations = this.properties.getProperty("PacMan.location").split(",");

    if(!trollLocations[0].isEmpty() && !trollLocations[1].isEmpty()) {
      int trollX = Integer.parseInt(trollLocations[0]);
      int trollY = Integer.parseInt(trollLocations[1]);
      addActor(troll, new Location(trollX, trollY), Location.NORTH);
    }
    if(!tx5Locations[0].isEmpty() && !tx5Locations[1].isEmpty()) {
      int tx5X = Integer.parseInt(tx5Locations[0]);
      int tx5Y = Integer.parseInt(tx5Locations[1]);
      addActor(tx5, new Location(tx5X, tx5Y), Location.NORTH);
    }
    if(!pacManLocations[0].isEmpty() && !pacManLocations[1].isEmpty()) {
      int pacManX = Integer.parseInt(pacManLocations[0]);
      int pacManY = Integer.parseInt(pacManLocations[1]);
      addActor(pacActor, new Location(pacManX, pacManY));
    }
//    int trollX = Integer.parseInt(trollLocations[0]);
//    int trollY = Integer.parseInt(trollLocations[1]);
//
//    int tx5X = Integer.parseInt(tx5Locations[0]);
//    int tx5Y = Integer.parseInt(tx5Locations[1]);
//
//    int pacManX = Integer.parseInt(pacManLocations[0]);
//    int pacManY = Integer.parseInt(pacManLocations[1]);
//
//    addActor(troll, new Location(trollX, trollY), Location.NORTH);
//    addActor(pacActor, new Location(pacManX, pacManY));
//    addActor(tx5, new Location(tx5X, tx5Y), Location.NORTH);
  }

  private int countPillsAndItems() {
    int pillsAndItemsCount = 0;
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a == 1 && propertyPillLocations.size() == 0) { // Pill
          pillsAndItemsCount++;
        } else if (a == 3 && propertyGoldLocations.size() == 0) { // Gold
          pillsAndItemsCount++;
        }
      }
    }
    if (propertyPillLocations.size() != 0) {
      pillsAndItemsCount += propertyPillLocations.size();
    }

    if (propertyGoldLocations.size() != 0) {
      pillsAndItemsCount += propertyGoldLocations.size();
    }

    return pillsAndItemsCount;
  }

  public ArrayList<Location> getPillAndItemLocations() {
    return pillAndItemLocations;
  }

  // load pill and items
  private void loadPillAndItemsLocations() {
    String pillsLocationString = properties.getProperty("Pills.location");
//    for (int i = 0; i < maze.length; i++) {
//      for (int j = 0; j < maze[0].length; j++) {
//        if (maze[i][j] == 1) {
//          propertyPillLocations.add(new Location(i,j));
//        } else if (maze[i][j] == 3) {
//          propertyGoldLocations.add(new Location(i,j));
//        }
//      }
//    }
    if (pillsLocationString != null && !pillsLocationString.equals("")) {
      String[] singlePillLocationStrings = pillsLocationString.split(";");
      for (String singlePillLocationString: singlePillLocationStrings) {
        String[] locationStrings = singlePillLocationString.split(",");
        propertyPillLocations.add(new Location(Integer.parseInt(locationStrings[0]), Integer.parseInt(locationStrings[1])));
      }
    }
//
    String goldLocationString = properties.getProperty("Gold.location");
    if (goldLocationString != null && !goldLocationString.equals("")) {
      String[] singleGoldLocationStrings = goldLocationString.split(";");
      for (String singleGoldLocationString: singleGoldLocationStrings) {
        String[] locationStrings = singleGoldLocationString.split(",");
        propertyGoldLocations.add(new Location(Integer.parseInt(locationStrings[0]), Integer.parseInt(locationStrings[1])));
      }
    }
  }
  //setup location
  private void setupPillAndItemsLocations() {
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a == 1 && propertyPillLocations.size() == 0) {
          pillAndItemLocations.add(location);
        }
        if (a == 3 &&  propertyGoldLocations.size() == 0) {
          pillAndItemLocations.add(location);
        }
        if (a == 4) {
          pillAndItemLocations.add(location);
        }
      }
    }


    if (propertyPillLocations.size() > 0) {
      for (Location location : propertyPillLocations) {
        pillAndItemLocations.add(location);
      }
    }
    if (propertyGoldLocations.size() > 0) {
      for (Location location : propertyGoldLocations) {
        pillAndItemLocations.add(location);
      }
    }
  }
  //draw map
  private void drawGrid(GGBackground bg)
  {
    bg.clear(Color.gray);
    bg.setPaintColor(Color.white);
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        bg.setPaintColor(Color.white);
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a > 0)
          bg.fillCell(location, Color.lightGray);
        //  && propertyPillLocations.size() == 0
        if (a == 1 && propertyPillLocations.size() == 0) { // Pill
          putPill(bg, location);
          //  &&  propertyGoldLocations.size() == 0
        } else if (a == 3 &&  propertyGoldLocations.size() == 0) { // Gold
          putGold(bg, location);
        } else if (a == 4) {  // Ice
          putIce(bg, location);
          //put other items
        } else if (a >= 5 && a <= 8) { // Portal
          putPortal(bg, location, a);
          //put other items, use hash to determine which item/switch
        }
      }
    }

    for (Location location : propertyPillLocations) {
      putPill(bg, location);
    }

    for (Location location : propertyGoldLocations) {
      putGold(bg, location);
    }
  }

  private void putPill(GGBackground bg, Location location){
    bg.setPaintColor(Color.white);
    bg.fillCircle(toPoint(location), 5);
  }

  private void putGold(GGBackground bg, Location location){
    bg.setPaintColor(Color.yellow);
    bg.fillCircle(toPoint(location), 5);
    Actor gold = new Actor("sprites/gold.png");
    this.goldPieces.add(gold);
    addActor(gold, location);
  }

  private void putIce(GGBackground bg, Location location){
    bg.setPaintColor(Color.blue);
    bg.fillCircle(toPoint(location), 5);
    Actor ice = new Actor("sprites/ice.png");
    this.iceCubes.add(ice);
    addActor(ice, location);
  }

  private void putPortal(GGBackground bg, Location location, int type){
    bg.setPaintColor(Color.black);
    bg.fillCircle(toPoint(location), 5);
    Actor portal;
    switch (type) {
      case 5:
        portal = new Actor("tiles/i_portalWhiteTile.png");
        break;
      case 6:
        portal = new Actor("tiles/j_portalYellowTile.png");
        break;
      case 7:
        portal = new Actor("tiles/k_portalDarkGoldTile.png");
        break;
      case 8:
        portal = new Actor("tiles/l_portalDarkGrayTile.png");
        break;
      default:
        portal = null;
        break;
    }
    addActor(portal, location);
  }

  public void removeItem(String type,Location location){
    if(type.equals("gold")){
      for (Actor item : this.goldPieces){
        if (location.getX() == item.getLocation().getX() && location.getY() == item.getLocation().getY()) {
          item.hide();
        }
      }
    }else if(type.equals("ice")){
      for (Actor item : this.iceCubes){
        if (location.getX() == item.getLocation().getX() && location.getY() == item.getLocation().getY()) {
          item.hide();
        }
      }
    }
  }

  public int getNumHorzCells(){
    return this.nbHorzCells;
  }
  public int getNumVertCells(){
    return this.nbVertCells;
  }
}
