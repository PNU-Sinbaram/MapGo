package com.sinbaram.mapgo

import android.content.Context
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.filament.ColorGrading
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.EngineInstance
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.lang.ref.WeakReference
import java.util.function.Consumer

/** AR Renderer class for MapGo Application */
class Renderer(context: Context, fragmentManager: FragmentManager) :
    BaseArFragment.OnTapArPlaneListener,
    BaseArFragment.OnSessionConfigurationListener,
    ArFragment.OnViewCreatedListener {

    // Main object for sceneform sdk
    lateinit var mArFragment: ArFragment

    // For sample gltf rendering
    var mModelRenderable: Renderable? = null

    // Application context for control
    val mContext: Context

    init {
        // Set context
        mContext = context
        if (Sceneform.isSupported(context)) {
            fragmentManager.beginTransaction()
                .add(R.id.arFragment, ArFragment::class.java, null)
                .commit()

            // Load sample gltf model for test rendernig
            loadModel("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/CesiumMan/glTF-Binary/CesiumMan.glb")
        }
    }

    fun attachFragment(fragment: Fragment) {
        mArFragment = fragment as ArFragment
        mArFragment.setOnSessionConfigurationListener(this)
        mArFragment.setOnViewCreatedListener(this)
        mArFragment.setOnTapArPlaneListener(this)
    }

    /** Create symbol node with given informations */
    fun createSymbolNode(x: Float, y: Float, z: Float, view: View): AnchorNode {
        val anchorNode = AnchorNode()
        anchorNode.worldPosition = Vector3(x, y, z)
        val cameraPos = mArFragment.arSceneView.scene.camera.worldPosition
        val direction = Vector3.subtract(cameraPos, anchorNode.worldPosition)
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
        anchorNode.worldRotation = lookRotation

        val titleNode = TransformableNode(mArFragment.getTransformationSystem())
        titleNode.setParent(anchorNode)
        titleNode.isEnabled = false
        titleNode.localPosition = Vector3(0.0f, 1.0f, 0.0f)

        ViewRenderable.builder()
            .setView(mContext, view)
            .build()
            .thenAccept(
                Consumer { viewRenderable: ViewRenderable ->
                    titleNode.setRenderable(viewRenderable)
                    titleNode.isEnabled = true
                }
            )
        return anchorNode
    }

    fun getScene(): Scene {
        return mArFragment.arSceneView.scene
    }

    override fun onTapPlane(hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent?) {
        // Create the Anchor.
        val anchor = hitResult!!.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(mArFragment.getArSceneView().getScene())

        // Create the transformable model and add it to the anchor.
        val model = TransformableNode(mArFragment.getTransformationSystem())
        model.setParent(anchorNode)
        model.setRenderable(mModelRenderable)
            .animate(true).start()
        model.select()

        val titleNode = Node()
        titleNode.setParent(model)
        titleNode.isEnabled = false
        titleNode.localPosition = Vector3(0.0f, 1.0f, 0.0f)
        titleNode.isEnabled = true
    }

    override fun onSessionConfiguration(session: Session?, config: Config?) {
        if (session!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
            config!!.setDepthMode(Config.DepthMode.AUTOMATIC)
    }

    override fun onViewCreated(arFragment: ArFragment?, arSceneView: ArSceneView?) {
        // Currently, the tone-mapping should be changed to FILMIC
        // because with other tone-mapping operators except LINEAR
        // the inverseTonemapSRGB function in the materials can produce incorrect results.
        // The LINEAR tone-mapping cannot be used together with the inverseTonemapSRGB function.
        val renderer = arSceneView!!.renderer

        if (renderer != null) {
            renderer.filamentView.colorGrading = ColorGrading.Builder()
                .toneMapping(ColorGrading.ToneMapping.FILMIC)
                .build(EngineInstance.getEngine().filamentEngine)
        }

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
    }

    /** Create model renderable from gltf model URL and view renderable in advance */
    private fun loadModel(modelUrl: String) {
        val weapRef: WeakReference<Renderer> = WeakReference(this)
        ModelRenderable.builder()
            .setSource(
                mContext,
                Uri.parse(modelUrl)
            )
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .build()
            .thenAccept { model: ModelRenderable ->
                val renderer: Renderer? = weapRef.get()
                if (renderer != null) {
                    renderer.mModelRenderable = model
                }
            }
            .exceptionally { throwable: Throwable? ->
                Toast.makeText(
                    mContext, "Unable to load model", Toast.LENGTH_LONG
                ).show()
                null
            }
    }
}
