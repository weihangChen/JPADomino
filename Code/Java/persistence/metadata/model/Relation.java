package persistence.metadata.model;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

/**
 * relation between persistent objects is saved in this class, for an example of
 * how to create a new Relation object, check
 * OneToManyRelationMetadataProcessor.java <br>
 * for example of how to read Relation object, check AbstractEntityReader.java
 * 
 * @author SWECWI
 * 
 */

public final class Relation {
	private Field property;
	private Class<?> targetEntity;
	private Class<?> propertyType;
	FetchType fetchType;
	private List<CascadeType> cascades;
	private boolean optional;
	private String mappedBy;
	private ForeignKey type;
	private String joinColumnName;
	private boolean isRelatedViaJoinTable;
	// private JoinTableMetadata joinTableMetadata;
	private boolean isJoinedByPrimaryKey;

	// domino specific
	private String dominoForeignKey;
	private String dominoView;
	private String dominoRelationSignature;

	public Relation(Field property, Class<?> targetEntity,
			Class<?> propertyType, FetchType fetchType,
			List<CascadeType> cascades, boolean optional, String mappedBy,
			ForeignKey type) {
		this.property = property;
		this.targetEntity = targetEntity;
		this.propertyType = propertyType;
		this.fetchType = fetchType;
		this.cascades = cascades;
		this.optional = optional;
		this.mappedBy = mappedBy;
		this.type = type;
	}

	public Field getProperty() {
		return this.property;
	}

	public Class<?> getTargetEntity() {
		return this.targetEntity;
	}

	public Class<?> getPropertyType() {
		return this.propertyType;
	}

	public FetchType getFetchType() {
		return this.fetchType;
	}

	public List<CascadeType> getCascades() {
		return this.cascades;
	}

	public boolean isOptional() {
		return this.optional;
	}

	public String getMappedBy() {
		return this.mappedBy;
	}

	public ForeignKey getType() {
		return this.type;
	}

	public String getJoinColumnName() {
		return this.joinColumnName;
	}

	public void setJoinColumnName(String joinColumnName) {
		this.joinColumnName = joinColumnName;
	}

	public boolean isRelatedViaJoinTable() {
		return this.isRelatedViaJoinTable;
	}

	public void setRelatedViaJoinTable(boolean isRelatedViaJoinTable) {
		this.isRelatedViaJoinTable = isRelatedViaJoinTable;
	}

	// public JoinTableMetadata getJoinTableMetadata()
	// /* */ {
	// /* 253 */ return this.joinTableMetadata;
	// /* */ }
	// /* */
	// /* */ public void setJoinTableMetadata(JoinTableMetadata
	// joinTableMetadata)
	// /* */ {
	// /* 264 */ this.joinTableMetadata = joinTableMetadata;
	// /* */ }

	public boolean isJoinedByPrimaryKey() {
		return this.isJoinedByPrimaryKey;
	}

	public void setJoinedByPrimaryKey(boolean isJoinedByPrimaryKey) {
		this.isJoinedByPrimaryKey = isJoinedByPrimaryKey;
	}

	public boolean isUnary() {
		return ((this.type.equals(ForeignKey.ONE_TO_ONE)) || (this.type
				.equals(ForeignKey.MANY_TO_ONE)));
	}

	public boolean isCollection() {
		return ((this.type.equals(ForeignKey.ONE_TO_MANY)) || (this.type
				.equals(ForeignKey.MANY_TO_MANY)));
	}

	public static enum ForeignKey {
		ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY;
	}

	public String getDominoForeignKey() {
		return dominoForeignKey;
	}

	public void setDominoForeignKey(String dominoForeignKey) {
		this.dominoForeignKey = dominoForeignKey;
	}

	public String getDominoView() {
		return dominoView;
	}

	public void setDominoView(String dominoView) {
		this.dominoView = dominoView;
	}

	public String getDominoRelationSignature() {
		return dominoRelationSignature;
	}

	public void setDominoRelationSignature(String dominoRelationSignature) {
		this.dominoRelationSignature = dominoRelationSignature;
	}

}
