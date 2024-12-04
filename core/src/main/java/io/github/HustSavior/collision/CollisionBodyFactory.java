package io.github.HustSavior.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.HustSavior.utils.GameConfig;

public class CollisionBodyFactory {
    private final World world;
    private final float PPM;

    public CollisionBodyFactory(World world, float PPM) {
        this.world = world;
        this.PPM = PPM;
    }

    public void createStaticBody(RectangleMapObject rectangleObject) {
        Rectangle rect = rectangleObject.getRectangle();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set((rect.x + rect.width / 2)/PPM, (rect.y + rect.height / 2)/PPM);

        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width / 2 / PPM, rect.height / 2 /PPM);

        body.createFixture(shape, 0.0f);
        shape.dispose();
    }

    public void createStaticBody(PolygonMapObject polygonObject) {
        try {
            Polygon polygon = polygonObject.getPolygon();
            float[] vertices = polygon.getTransformedVertices();
            Vector2[] worldVertices = new Vector2[vertices.length / 2];
    
            for (int i = 0; i < vertices.length / 2; i++) {
                worldVertices[i] = new Vector2(
                    vertices[i * 2] / GameConfig.PPM,
                    vertices[i * 2 + 1] / GameConfig.PPM
                );
            }
    
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(0, 0);
    
            Body body = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.set(worldVertices);
    
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1.0f;
            fixtureDef.friction = 0.4f;
            fixtureDef.restitution = 0.0f;
    
            body.createFixture(fixtureDef);
            shape.dispose();
    
            Gdx.app.log("CollisionBodyFactory", "Created polygon body with " + worldVertices.length + " vertices");
        } catch (Exception e) {
            Gdx.app.error("CollisionBodyFactory", "Failed to create polygon body", e);
        }
    }
} 