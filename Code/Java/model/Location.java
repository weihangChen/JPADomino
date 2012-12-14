package model;

//import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

import persistence.annotation.DocumentReferences;
import persistence.annotation.DominoEntity;
import persistence.annotation.DominoProperty;
import model.notes.ModelBase;

/**
 * annotated pojo class
 * 
 * @author weihang chen
 * 
 */
@DominoEntity(formName = "Location", viewName = "Location")
public class Location extends ModelBase {
	public Location() {
		toolBoxEagerList = new HashSet<ToolBox>();
	}

	// // eager, collection concrete class, with cascade
	// @DocumentReferences(fetch = FetchType.LAZY, foreignKey = "unid", viewName
	// = "CSS", cascade = { CascadeType.SAVE_UPDATE })
	// private ArrayList<ToolBox> toolBoxLazyList;
	// lazy, collection interface, without cascade
	@DocumentReferences(fetch = FetchType.EAGER, foreignKey = "unid", viewName = "ToolBox", cascade = { CascadeType.ALL })
	private Set<ToolBox> toolBoxEagerList;

	public Location(Object obj) {
		super(obj);
		toolBoxEagerList = new HashSet<ToolBox>();
	}

	@DominoProperty(itemName = "locationCapcity")
	protected int locationCapcity;

	public int getLocationCapcity() {
		return locationCapcity;
	}

	public void setLocationCapcity(int locationCapcity) {
		this.locationCapcity = locationCapcity;
	}

	// public ArrayList<ToolBox> getToolBoxLazyList() {
	// return toolBoxLazyList;
	// }
	//
	// public void setToolBoxLazyList(ArrayList<ToolBox> toolBoxLazyList) {
	// this.toolBoxLazyList = toolBoxLazyList;
	// }

	public Set<ToolBox> getToolBoxEagerList() {
		return toolBoxEagerList;
	}

	public void setToolBoxEagerList(Set<ToolBox> toolBoxEagerList) {
		this.toolBoxEagerList = toolBoxEagerList;
	}

}
