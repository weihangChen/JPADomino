package persistence.metadata.processor.relation;

import persistence.annotation.DocumentReferences;

import persistence.metadata.model.EntityMetadata;
import persistence.metadata.model.Relation;
import persistence.metadata.processor.AbstractEntityFieldProcessor;
import persistence.property.PropertyAccessorHelper;
import util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * DocumentReferences annotation stands for OneToMany relation, annotation value is
 * used to build Relation objects, which are added to EntityMetadata object
 * 
 * @author weihang chen
 * 
 */
public class OneToManyRelationMetadataProcessor extends
		AbstractEntityFieldProcessor implements RelationMetadataProcessor {
	public OneToManyRelationMetadataProcessor() {
		// this.validator = new EntityValidatorImpl();
	}

	public void process(Class<?> clazz, EntityMetadata metadata) {
		throw new RuntimeException(
				"Method call not applicable for Relation processors");
	}

	public void addRelationIntoMetadata(Field relationField,
			EntityMetadata metadata) {
		DocumentReferences ann = (DocumentReferences) relationField
				.getAnnotation(DocumentReferences.class);

		Class<?> targetEntity = PropertyAccessorHelper
				.getGenericClass(relationField);

		if ((null == targetEntity)
				|| ((targetEntity.getSimpleName().equals("void"))))
			return;

		validate(targetEntity);
		// mappedBy Required unless the relationship is unidirectional.
		Relation relation = new Relation(relationField, targetEntity,
				relationField.getType(), ann.fetch(), Arrays.asList(ann
						.cascade()), Boolean.TRUE.booleanValue(), "MAPPEDBY",
				Relation.ForeignKey.ONE_TO_MANY);
		relation.setDominoForeignKey(ann.foreignKey());
		relation.setDominoView(ann.viewName());
		relation.setDominoRelationSignature(ann.foreignKey() + ann.viewName()
				+ ann.fetch().name().toString());

		metadata.addRelation(relationField.getName(), relation);
		System.out.println("METHOD SIGNATURE: "
				+ CommonUtil.getMethodName(this.getClass().toString())
				+ " /METHOD DESCRIPTION: added relation is: "
				+ relationField.getName() + " /" + relation.getFetchType()
				+ "/" + relation.getTargetEntity() + "/"
				+ relation.getJoinColumnName());
		metadata.setParent(true);
	}

}
