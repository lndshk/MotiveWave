package study_examples;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.DrawContext;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.ResizeType;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.PathInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.menu.MenuDescriptor;
import com.motivewave.platform.sdk.common.menu.MenuItem;
import com.motivewave.platform.sdk.common.menu.MenuSeparator;
import com.motivewave.platform.sdk.draw.Figure;
import com.motivewave.platform.sdk.draw.ResizePoint;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** This study draws a trend line on the price graph and allows the user to move it using the resize points. 
    The purpose of this example is to demonstrate advanced features such as using resize points and context menus. */
@StudyHeader(
    namespace="com.motivewave", 
    id="TREND_LINE",
    rb="study_examples.nls.strings", // locale specific strings are loaded from here
    name="Trend Line",
    desc="This is an example study that draws a simple trend line and allows the user to resize it",
    menu="MENU_EXAMPLES",
    overlay=true)
public class TrendLine extends com.motivewave.platform.sdk.study.Study 
{
  final static String START="start", END="end";
  final static String EXT_RIGHT="extRight", EXT_LEFT="extLeft";
  
  @Override
  public void initialize(Defaults defaults)
  {
    SettingsDescriptor sd=new SettingsDescriptor();
    SettingTab tab=new SettingTab("General");
    sd.addTab(tab);
    setSettingsDescriptor(sd);

    SettingGroup grp=new SettingGroup("");
    grp.addRow(new PathDescriptor(Inputs.PATH, "Line", defaults.getLineColor(), 1.0f, null, true, false, true));
    grp.addRow(new BooleanDescriptor(EXT_RIGHT, "Extend Right", false));
    grp.addRow(new BooleanDescriptor(EXT_LEFT, "Extend Left", false));

    tab.addGroup(grp);

    RuntimeDescriptor desc=new RuntimeDescriptor();
    setRuntimeDescriptor(desc);
    
  }
  
  @Override
  public void onLoad(Defaults defaults)
  {
    // Initialize the resize points and the trend line figure
    startResize = new ResizePoint(ResizeType.ALL, true);
    startResize.setSnapToLocation(true);
    endResize = new ResizePoint(ResizeType.ALL, true);
    endResize.setSnapToLocation(true);
    trendLine = new Line();
  }

  // Adds custom menu items to the context menu when the user right clicks on this study.
  @Override
  public MenuDescriptor onMenu(String plotName, Point loc, DrawContext ctx)
  {
    List<MenuItem> items = new ArrayList<>();
    items.add(new MenuSeparator());
    // Add some menu items for the user to extend right and left without having to open the study dialog
    boolean extLeft = getSettings().getBoolean(EXT_LEFT);
    boolean extRight = getSettings().getBoolean(EXT_RIGHT);
    
    // Note: the study will be recalculated (ie call calculateValues(), see below) when either of these menu items is invoked by the user
    items.add(new MenuItem("Extend Left", extLeft, () -> getSettings().setBoolean(EXT_LEFT, !extLeft)));
    items.add(new MenuItem("Extend Right", extRight, () -> getSettings().setBoolean(EXT_RIGHT, !extRight)));
    return new MenuDescriptor(items, true);
  }

  // This method is called when the user is moving a resize point but has not released the mouse button yet.
  // This does not cause the study to be recalculated until the resize operation is completed.
  @Override
  public void onResize(ResizePoint rp, DrawContext ctx)
  {
    // In our case we want to adjust the trend line as the user moves the resize point
    // This will provide visual feedback to the user
    trendLine.layout(ctx);
  }

  // This method is called when the user has completed moving a resize point with the mouse.
  // The underlying study framework will recalculate the study after this method is called.
  @Override
  public void onEndResize(ResizePoint rp, DrawContext ctx)
  {
    // Commit the resize to the study settings, so it can be used in calculateValues() (see below)
    // We will store this in the settings as a string: "<price>|<time in millis>"
    getSettings().setString(rp == startResize ? START : END, rp.getValue() + "|" + rp.getTime());
  }
  
  // This method is called whenever the study needs to be (re)calculated.
  // In this example, this will be when the study loads, bar size changes as well as calling the onEndResize() method.
  // Additionally, the study will be recalculated every time a custom menu item is invoked.
  @Override
  protected void calculateValues(DataContext ctx)
  {
    // If the points have not been defined yet, just choose two...
    DataSeries series = ctx.getDataSeries();
    long startTime = series.getStartTime(series.size()-41);
    double startPrice = series.getDouble(series.size()-41, Enums.BarInput.MIDPOINT);
    long endTime = series.getStartTime(series.size()-1);
    double endPrice = series.getFloat(series.size()-1, Enums.BarInput.MIDPOINT);

    // Storage format is price|time
    String start = getSettings().getString(START);
    String end = getSettings().getString(END);
    if (!Util.isEmpty(start)) {
      startPrice = Double.valueOf(start.substring(0, start.indexOf('|')));
      startTime = Long.valueOf(start.substring(start.indexOf('|')+1));
    }
    if (!Util.isEmpty(end)) {
      endPrice = Double.valueOf(end.substring(0, end.indexOf('|')));
      endTime = Long.valueOf(end.substring(end.indexOf('|')+1));
    }
    
    startResize.setLocation(startTime, startPrice);
    endResize.setLocation(endTime, endPrice);

    // Figures are cleared when the study is recalculated, so we need to add these figures every time
    addFigure(trendLine);
    addFigure(startResize);
    addFigure(endResize);
  }
  
  // This class is responsible for the rendering of the trend line
  // It also allows for user selection
  private class Line extends Figure
  {
    Line() {}
    
    // This method enables the user to select the line when the click on it.
    // We will check to see if the mouse is 6 pixels away from the trend line
    @Override
    public boolean contains(double x, double y, DrawContext ctx)
    {
      return line != null && Util.distanceFromLine(x, y, line) < 6;
    }

    // Create a line using the start and end resize points.
    // We also need to handle the cases where the user extends the line to the right and/or left
    @Override
    public void layout(DrawContext ctx)
    {
      Point2D start = ctx.translate(startResize.getLocation());
      Point2D end = ctx.translate(endResize.getLocation());
      // Its possible that the start resize point has been moved past the end resize
      // If this is the case, just reverse the points...
      if (start.getX() > end.getX()) {
        Point2D tmp = end;
        end = start;
        start = tmp;
      }
      
      Rectangle gb = ctx.getBounds(); // this is the bounds of the graph
      double m = Util.slope(start, end); // calculate the slope, using a utility function for this
      
      if (getSettings().getBoolean(EXT_LEFT)) start = calcPoint(m, end, gb.getX(), gb);
      if (getSettings().getBoolean(EXT_RIGHT)) end = calcPoint(m, start, gb.getMaxX(), gb);

      line = new Line2D.Double(start, end);
    }

    // Extend the line to the given x coordinate using the simple slope formula: y = mx + b
    // This also handles the case where we have a vertical line (infinite slope)
    private Point2D calcPoint(double m, Point2D p, double x, Rectangle gb)
    {
      double y = 0;
      if (m == Double.POSITIVE_INFINITY) {
        y = gb.getMaxY();
        x = p.getX();
      }
      else if ( m == Double.NEGATIVE_INFINITY) {
        y = gb.getMinY();
        x = p.getX();
      }
      else {
        // y = mx + b
        double b = p.getY() - (m * p.getX());
        y = m*x + b;
      }
      return new Point2D.Double(x, y);
    }

    // Draw the line using the settings in the PATH variable
    @Override
    public void draw(Graphics2D gc, DrawContext ctx)
    {
      PathInfo path = getSettings().getPath(Inputs.PATH);
      // Provide feedback to the user by making the line bolder when it is selected by the user (ie use the selected stroke).
      gc.setStroke(ctx.isSelected() ? path.getSelectedStroke() : path.getStroke());
      gc.setColor(path.getColor());
      gc.draw(line);
    }
    
    private Line2D line;
  }
  
  private ResizePoint startResize, endResize;
  private Line trendLine;
}
