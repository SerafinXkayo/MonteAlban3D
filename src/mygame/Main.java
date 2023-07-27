package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.BulletAppState;
import  com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls Based on JMonkeyEngine's
 * SimpleApplication. Original author: normenhansen
 */
public class Main extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private BitmapText infoMovimiento;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(0.0f));
        sun.setEnabled(true);
        rootNode.addLight(sun);
        flyCam.setMoveSpeed(20);

        Spatial casona;
        casona = assetManager.loadModel("Models/MonteAlbanModel/MonteAlbanModel.j3o");
        casona.setLocalScale(2f);
        rootNode.attachChild(casona);

        /**
         * Set up Physics
         */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        setUpKeys();
        setUpLight();
        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) casona);
        landscape = new RigidBodyControl(sceneShape, 0);

        casona.addControl(landscape);
        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.4f, 2f, 1);
        player = new CharacterControl(capsuleShape, 0.4f);
        player.setJumpSpeed(30);
        player.setFallSpeed(500);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(casona);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);

        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        infoMovimiento = new BitmapText(guiFont, false);
        //infoMovimiento.setSize(guiFont.getCharSet().getRenderedSize());//Tamañoletra
        infoMovimiento.setSize(30);
        infoMovimiento.setColor(ColorRGBA.Red);
        guiNode.attachChild(infoMovimiento);

        infoMovimiento.setLocalTranslation(10, cam.getHeight() - 10, 0);
        infoMovimiento.setText("Controles de navegacion\n"
                + "Presiona ( W ): Para ir hacia adelante\n"
                + "Presiona ( A ): Para moverte a la izquierda\n"
                + "Presiona ( D ): Para moverte a la derecha\n"
                + "Presiona ( S ): Para ir hacia atras\n"
                + "Presiona ( Espacio ): Para saltar\n"
                + "Usa el raton para mover la camara");

    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    /**
     * We over-write some navigational key mappings here, so we can add
     * physics-controlled walking and jumping:
     */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump");
    }

    /**
     * These are our custom actions triggered by key presses. We do not walk
     * yet, we just keep track of the direction the user pressed.
     */
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            left = value;
        } else if (binding.equals("Right")) {
            right = value;
        } else if (binding.equals("Up")) {
            up = value;
        } else if (binding.equals("Down")) {
            down = value;
        } else if (binding.equals("Jump") && value) {
            player.jump();
        }
    }

    /**
     * This is the main event loop--walking happens here. We check in which
     * direction the player is walking by interpreting the camera direction
     * forward (camDir) and to the side (camLeft). The setWalkDirection()
     * command is what lets a physics-controlled player walk. We also make sure
     * here that the camera moves with the player.
     */
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.2f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }
}