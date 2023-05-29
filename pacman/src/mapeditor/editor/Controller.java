package src.mapeditor.editor;

import src.Game;
import src.mapeditor.grid.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import src.utility.GameCallback;
import src.utility.PropertiesLoader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Controller of the application.
 * 
 * @author Daniel "MaTachi" Jonsson
 * @version 1
 * @since v0.0.5
 * 
 */
public class Controller implements ActionListener, GUIInformation {

	/**
	 * The model of the map editor.
	 */
	private Grid model;

	private Tile selectedTile;
	private Camera camera;

	private List<Tile> tiles;

	private GridView grid;
	private View view;

	private StringBuilder stringBuilder;
	private int gridWith = Constants.MAP_WIDTH;
	private int gridHeight = Constants.MAP_HEIGHT;

	private Properties my_properties;
	/**
	 * Construct the controller.
	 */
	public Controller() {
		init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);

	}

	public void init(int width, int height) {
		this.tiles = TileManager.getTilesFromFolder("tiles/");
		this.model = new GridModel(width, height, tiles.get(0).getCharacter());
		this.camera = new GridCamera(model, Constants.GRID_WIDTH,
				Constants.GRID_HEIGHT);

		grid = new GridView(this, camera, tiles); // Every tile is
													// 30x30 pixels

		this.view = new View(this, camera, grid, tiles);
		String propertiesPath = "properties/test2.properties";
		this.my_properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
	}

	/**
	 * Different commands that comes from the view.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		for (Tile t : tiles) {
			if (e.getActionCommand().equals(
					Character.toString(t.getCharacter()))) {
				selectedTile = t;
				break;
			}
		}
		if (e.getActionCommand().equals("flipGrid")) {
			// view.flipGrid();
		} else if (e.getActionCommand().equals("save")) {
			stringBuilder = new StringBuilder();
			this.my_properties = saveFile();
		} else if (e.getActionCommand().equals("load")) {
			stringBuilder = new StringBuilder();
			this.my_properties = loadFile();
		} else if (e.getActionCommand().equals("update")) {
			updateGrid(gridWith, gridHeight);
		} else if(e.getActionCommand().equals("startGame")) {
			if(!Boolean.parseBoolean(this.my_properties.getProperty("isValid"))) {
				JOptionPane.showMessageDialog(null,"Map Error,Please see log! ");
				return;
			}
			if(this.my_properties.getProperty("PacMan.location").isEmpty()) {
				JOptionPane.showMessageDialog(null,"pacman's location is null");
				return;
			}
			// start game
			GameCallback gameCallback = new GameCallback();
			if(stringBuilder == null || stringBuilder.toString().isEmpty()) {
				new Game(gameCallback, this.my_properties,null);
			} else {
				new Game(gameCallback,this.my_properties,stringBuilder);
			}
		}
	}

	public void updateGrid(int width, int height) {
		view.close();
		init(width, height);
		view.setSize(width, height);
	}

	DocumentListener updateSizeFields = new DocumentListener() {

		public void changedUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}

		public void removeUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}

		public void insertUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}
	};

	private Properties saveFile() {
		String propertiesPath = "properties/template.properties";
		Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"xml files", "xml");
		chooser.setFileFilter(filter);
		File workingDirectory = new File(System.getProperty("user.dir"));
		chooser.setCurrentDirectory(workingDirectory);

		int returnVal = chooser.showSaveDialog(null);
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				Element level = new Element("level");
				Document doc = new Document(level);
				doc.setRootElement(level);

				Element size = new Element("size");
				int height = model.getHeight();
				int width = model.getWidth();
				size.addContent(new Element("width").setText(width + ""));
				size.addContent(new Element("height").setText(height + ""));
				doc.getRootElement().addContent(size);
				String tx5_location = "";
				String troll_location = "";
				String pacman_location = "";
				String pill_localtion = "";
				String gold_location = "";
				String ice_location = "";
				String portalWhiteTile_location = "";
				String portalYellowTile_location= "";
				String portalDarkGrayTile_location= "";
				String portalDarkGoldTile_location= "";

				for (int y = 0; y < height; y++) {
					Element row = new Element("row");
					for (int x = 0; x < width; x++) {
						String type = "";
						char tileChar = model.getTile(x,y);
						if (tileChar == 'a') {
							type = "PathTile";
							stringBuilder.append(" ");
						}
						if (tileChar == 'b'){
							type = "WallTile";
							stringBuilder.append("x");
						}
						else if (tileChar == 'c') {
							type = "PillTile";
							stringBuilder.append(".");
							pill_localtion += x + "," + y + ";";
						}
						else if (tileChar == 'd') {
							type = "GoldTile";
							stringBuilder.append("g");
							gold_location += x + "," + y + ";";
						}
						else if (tileChar == 'e') {
							type = "IceTile";
							stringBuilder.append("i");
							ice_location += x + "," + y + ";";
						}
						else if (tileChar == 'f') {
							type = "PacTile";
							stringBuilder.append("f");
							pacman_location += x + "," + y + ";";
						}
						else if (tileChar == 'g') {
							type = "TrollTile";
							stringBuilder.append("t");
							troll_location += x + "," + y + ";";
						}
						else if (tileChar == 'h') {
							type = "TX5Tile";
							stringBuilder.append("h");
							tx5_location += x + "," + y + ";";
						}
						else if (tileChar == 'i') {
							type = "PortalWhiteTile";
							stringBuilder.append("w");
							portalWhiteTile_location += x + "," + y + ";";
						}
						else if (tileChar == 'j') {
							type = "PortalYellowTile";
							stringBuilder.append("y");
							portalYellowTile_location += x + "," + y + ";";
						}
						else if (tileChar == 'k') {
							type = "PortalDarkGoldTile";
							stringBuilder.append("D");
							portalDarkGoldTile_location += x + "," + y + ";";
						}
						else if (tileChar == 'l') {
							type = "PortalDarkGrayTile";
							stringBuilder.append("G");
							portalDarkGrayTile_location += x + "," + y + ";";
						}

						Element e = new Element("cell");
						row.addContent(e.setText(type));
					}
					doc.getRootElement().addContent(row);
				}
				XMLOutputter xmlOutput = new XMLOutputter();
				xmlOutput.setFormat(Format.getPrettyFormat());
				xmlOutput
						.output(doc, new FileWriter(chooser.getSelectedFile()));
				if(!isComplete(portalWhiteTile_location,"PortalWhiteTile") ||
						!isComplete(portalYellowTile_location,"PortalYellowTile") ||
						!isComplete(portalDarkGrayTile_location,"PortalDarkGoldTile") ||
						!isComplete(portalDarkGoldTile_location,"PortalDarkGrayTile")) {
					properties.setProperty("isValid","false");
				} else {
					properties.setProperty("isValid","true");
				}
				properties.setProperty("PacMan.location", removeLastCharacter(pacman_location));
				properties.setProperty("TX5.location", removeLastCharacter(tx5_location));
				properties.setProperty("Troll.location", removeLastCharacter(troll_location));
				properties.setProperty("Pills.location", removeLastCharacter(pill_localtion));
				properties.setProperty("Gold.location", removeLastCharacter(gold_location));
				properties.setProperty("Ice.location", removeLastCharacter(ice_location));
				properties.setProperty("PortalWhiteTile.location", removeLastCharacter(portalWhiteTile_location));
				properties.setProperty("PortalYellowTile.location", removeLastCharacter(portalYellowTile_location));
				properties.setProperty("PortalDarkGrayTile.location", removeLastCharacter(portalDarkGrayTile_location));
				properties.setProperty("PortalDarkGoldTile.location", removeLastCharacter(portalDarkGoldTile_location));
			}
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid file!", "error",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
		}
		return properties;
	}

	public Properties loadFile() {
		String propertiesPath = "properties/template.properties";
		Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
		SAXBuilder builder = new SAXBuilder();
		try {
			JFileChooser chooser = new JFileChooser();
			File selectedFile;
			BufferedReader in;
			FileReader reader = null;
			File workingDirectory = new File(System.getProperty("user.dir"));
			chooser.setCurrentDirectory(workingDirectory);

			int returnVal = chooser.showOpenDialog(null);
			Document document;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selectedFile = chooser.getSelectedFile();
				if (selectedFile.canRead() && selectedFile.exists()) {
					document = (Document) builder.build(selectedFile);

					Element rootNode = document.getRootElement();

					List sizeList = rootNode.getChildren("size");
					Element sizeElem = (Element) sizeList.get(0);
					int height = Integer.parseInt(sizeElem
							.getChildText("height"));
					int width = Integer
							.parseInt(sizeElem.getChildText("width"));
					updateGrid(width, height);
					String tx5_location = "";
					String troll_location = "";
					String pacman_location = "";
					String pill_localtion = "";
					String gold_location = "";
					String ice_location = "";
					String portalWhiteTile_location = "";
					String portalYellowTile_location= "";
					String portalDarkGrayTile_location= "";
					String portalDarkGoldTile_location= "";

					List rows = rootNode.getChildren("row");
					for (int y = 0; y < rows.size(); y++) {
						Element cellsElem = (Element) rows.get(y);
						List cells = cellsElem.getChildren("cell");

						for (int x = 0; x < cells.size(); x++) {
							Element cell = (Element) cells.get(x);
							String cellValue = cell.getText();

							char tileNr = 'a';
							if (cellValue.equals("PathTile")) {
								stringBuilder.append(" ");
								tileNr = 'a';
							}
							else if (cellValue.equals("WallTile")){
								tileNr = 'b';
								stringBuilder.append("x");
							}
							else if (cellValue.equals("PillTile")){
								tileNr = 'c';
								stringBuilder.append(".");
								pill_localtion += x + "," + y + ";";
							}
							else if (cellValue.equals("GoldTile")){
								tileNr = 'd';
								stringBuilder.append("g");
								gold_location += x + "," + y + ";";
							}
							else if (cellValue.equals("IceTile")) {
								tileNr = 'e';
								stringBuilder.append("i");
								ice_location += x + "," + y + ";";
							}
							else if (cellValue.equals("PacTile")) {
								tileNr = 'f';
								stringBuilder.append("f");
								pacman_location += x + "," + y + ";";
							}
							else if (cellValue.equals("TrollTile")) {
								tileNr = 'g';
								stringBuilder.append("t");
								troll_location += x + "," + y + ";";
							}
							else if (cellValue.equals("TX5Tile")) {
								tileNr = 'h';
								stringBuilder.append("h");
								tx5_location += x + "," + y + ";";
							}
							else if (cellValue.equals("PortalWhiteTile")) {
								tileNr = 'i';
								stringBuilder.append("w");
								portalWhiteTile_location += x + "," + y + ";";
							}
							else if (cellValue.equals("PortalYellowTile")) {
								tileNr = 'j';
								stringBuilder.append("y");
								portalYellowTile_location += x + "," + y + ";";
							}
							else if (cellValue.equals("PortalDarkGoldTile")) {
								tileNr = 'k';
								stringBuilder.append("D");
								portalDarkGoldTile_location += x + "," + y + ";";
							}
							else if (cellValue.equals("PortalDarkGrayTile")) {
								tileNr = 'l';
								stringBuilder.append("G");
								portalDarkGrayTile_location += x + "," + y + ";";
							}
							else {
								tileNr = '0';
							}

							model.setTile(x, y, tileNr);
						}
					}

					if(!isComplete(portalWhiteTile_location,"PortalWhiteTile") ||
							!isComplete(portalYellowTile_location,"PortalYellowTile") ||
							!isComplete(portalDarkGrayTile_location,"PortalDarkGoldTile") ||
							!isComplete(portalDarkGoldTile_location,"PortalDarkGrayTile")) {
						properties.setProperty("isValid","false");
					} else {
						properties.setProperty("isValid","true");
					}

					properties.setProperty("PacMan.location", removeLastCharacter(pacman_location));
					properties.setProperty("TX5.location", removeLastCharacter(tx5_location));
					properties.setProperty("Troll.location", removeLastCharacter(troll_location));
					properties.setProperty("Pills.location", removeLastCharacter(pill_localtion));
					properties.setProperty("Gold.location", removeLastCharacter(gold_location));
					properties.setProperty("Ice.location", removeLastCharacter(ice_location));
					properties.setProperty("PortalWhiteTile.location", removeLastCharacter(portalWhiteTile_location));
					properties.setProperty("PortalYellowTile.location", removeLastCharacter(portalYellowTile_location));
					properties.setProperty("PortalDarkGrayTile.location", removeLastCharacter(portalDarkGrayTile_location));
					properties.setProperty("PortalDarkGoldTile.location", removeLastCharacter(portalDarkGoldTile_location));
					String mapString = model.getMapAsString();
					grid.redrawGrid();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getSelectedTile() {
		return selectedTile;
	}

	private String  removeLastCharacter(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, str.length() - 1);
	}

	private boolean isComplete(String location,String content) throws IOException{
		int sum = 0;
		boolean result;
		char[] chars= location.toCharArray();
		for(char a : chars) {
			if (a == ';') {
				sum++;
			}
		}
		if(sum == 2 || sum ==0) {
			result = true;
		} else {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String filePath = "./log/error.txt";
			String log = simpleDateFormat.format(new Date(System.currentTimeMillis())) +"   " + content + "location error";
			try (FileWriter fileWriter = new FileWriter(filePath);
				 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
				 bufferedWriter.write(log);
			}
			result =  false;
		}
		return result;
	}

}
