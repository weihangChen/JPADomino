package model;

import model.notes.ModelBase;
import persistence.annotation.DominoEntity;
import persistence.annotation.DominoProperty;

/**
 * annotated pojo class
 * 
 * @author weihang chen
 * 
 */
@DominoEntity(formName = "ToolBox", viewName = "ToolBox")
public class ToolBox extends ModelBase {
	@DominoProperty(itemName = "locationUNID")
	protected String locationUNID;
	@DominoProperty(itemName = "toolboxWeight")
	protected String toolboxWeight;
	@DominoProperty(itemName = "toolboxHeight")
	protected String toolboxHeight;

	public ToolBox() {

	}

	public ToolBox(Object doc) {
		super(doc);
	}

	public String getLocationUNID() {
		return locationUNID;
	}

	public void setLocationUNID(String locationUNID) {
		this.locationUNID = locationUNID;
	}

	public String getToolboxWeight() {
		return toolboxWeight;
	}

	public void setToolboxWeight(String toolboxWeight) {
		this.toolboxWeight = toolboxWeight;
	}

	public String getToolboxHeight() {
		return toolboxHeight;
	}

	public void setToolboxHeight(String toolboxHeight) {
		this.toolboxHeight = toolboxHeight;
	}

}
