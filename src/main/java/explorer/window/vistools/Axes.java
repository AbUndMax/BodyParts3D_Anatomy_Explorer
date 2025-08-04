package explorer.window.vistools;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

/**
 * Contains three Cylinders.
 * They are of equal length and different color (Green, Red, Blue).
 *
 * SOURCE: The whole class was copied from assignment 06!
 */
public class Axes extends Group {

    public Axes(double length) {

        //X-axis
        this.getChildren().add(
                rotateShape3D(
                        makeCylinderVec(length, makePhongMaterialW(Color.RED)),
                        new Point3D(0,0,1),
                         90.0
        ));
        //Z-axis
        this.getChildren().add(
                rotateShape3D(
                        makeCylinderVec(length, makePhongMaterialW(Color.BLUE)),
                        new Point3D(1,0,0),
                        90.0
        ));
        //Y-axis
        this.getChildren().add(
                makeCylinderVec(length, makePhongMaterialW(Color.GREEN))
                );

        this.getChildren().get(0).setTranslateX(length/2);
        this.getChildren().get(1).setTranslateZ(length/2);
        this.getChildren().get(2).setTranslateY(-length/2);

    }

    /**
     * Rotates a Shape3D around an axis.
     * @param shape: your shape.
     * @param axis: axis as Point3D.
     * @param angle: angle as double in degrees.
     * @return your rotated shape.
     */
    public Shape3D rotateShape3D(Shape3D shape, Point3D axis, double angle) {
        Rotate rotate = new Rotate(angle, axis);

        if (shape.getTransforms().isEmpty()) {
            shape.getTransforms().add(rotate);
        }
        else {
            var currentTransform = shape.getTransforms().getFirst();
            currentTransform = rotate.createConcatenation(currentTransform);
            shape.getTransforms().setAll(currentTransform);
        }
        return shape;
    }

    /**
     * Makes Cylinder of length l and radius equal to 7.5% of length.
     * Applies material to cylinder.
     * Sets coordinates (0,0,0)
     */
    private Cylinder makeCylinderVec(double length, PhongMaterial material) {
        Cylinder cylinder = new Cylinder(length * 0.075, length);
        cylinder.setMaterial(material);
        cylinder.setTranslateX(0);
        cylinder.setTranslateY(0);
        cylinder.setTranslateZ(0);
        return cylinder;
    }

    /**
     * Makes PhongMaterial of desired color and SpecularColor WHITE.
     */
    private PhongMaterial makePhongMaterialW(Color color) {
        PhongMaterial phongMaterial = new PhongMaterial(color);
        phongMaterial.setSpecularColor(Color.WHITE);
        return phongMaterial;
    }
}
