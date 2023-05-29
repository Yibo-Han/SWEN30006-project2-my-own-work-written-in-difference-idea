// PacGrid.java
package src;

import ch.aplu.jgamegrid.*;

public class PacManGameGrid
{
  private StringBuilder stringBuilder;
  private int nbHorzCells;
  private int nbVertCells;
  private int[][] mazeArray;

  public void setStringBuilder(StringBuilder stringBuilder) {
    this.stringBuilder = stringBuilder;
  }

  public PacManGameGrid(int nbHorzCells, int nbVertCells)
  {
    this.nbHorzCells = nbHorzCells;
    this.nbVertCells = nbVertCells;
    String maze;
    mazeArray = new int[nbVertCells][nbHorzCells];
    maze =    "xxxxxxxxxxxxxxxxxxxx" + // 0
              "x....x....g...x....x" + // 1
              "xgxx.x.xxxxxx.x.xx.x" + // 2
              "x.x.......i.g....x.x" + // 3
              "x.x.xx.xx  xx.xx.x.x" + // 4
              "x......x    x......x" + // 5
              "x.x.xx.xxxxxx.xx.x.x" + // 6
              "x.x......gi......x.x" + // 7
              "xixx.x.xxxxxx.x.xx.x" + // 8
              "x...gx....g...x....x" + // 9
              "xxxxxxxxxxxxxxxxxxxx";// 10
    // Copy structure into integer array
    for (int i = 0; i < nbVertCells; i++)
    {
      for (int k = 0; k < nbHorzCells; k++) {
        int value = toInt(maze.charAt(nbHorzCells * i + k));
        mazeArray[i][k] = value;
      }
    }
  }

  public void setMazeArray(StringBuilder builder) {
    String s  = builder.toString();
    mazeArray = new int[nbVertCells][nbHorzCells];
    for (int i = 0; i < nbVertCells; i++)
    {
      for (int k = 0; k < nbHorzCells; k++) {
        int value = toInt(s.charAt(nbHorzCells * i + k));
        mazeArray[i][k] = value;
      }
    }
  }

  public int getCell(Location location)
  {
    return mazeArray[location.y][location.x];
  }
  private int toInt(char c)
  {
    if (c == 'x') // WallTile
      return 0;
    if (c == '.') // PillTile
      return 1;
    if (c == ' ') //gap empty
      return 2;
    if (c == 'g') // GoldTile
      return 3;
    if (c == 'i') // IceTile
      return 4;
    if (c == 'f') // PacTile
      return 9;
    if (c == 't') // TrollTile
      return 9;
    if (c == 'h') // TX5Tile
      return 9;
    if (c == 'w') // PortalWhiteTile
      return 5;
    if (c == 'y') // PortalYellowTile
      return 6;
    if (c == 'D') // PortalDarkGoldTile
      return 7;
    if (c == 'G') // PortalDarkGrayTile
      return 8;
    return -1;
  }
}
