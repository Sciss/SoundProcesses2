package de.sciss.synth.proc
package view

import javax.swing._
import event.{ChangeEvent, ChangeListener}
import GUIUtils._

class MomentaryProcView[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( sys: System[ C, V ],
                                                        p: Proc[ C, V ], csr: Cursor[ C, V ]) {

   val frame = {
      val f          = new JFrame( "Proc : " + p.name )
      val cp         = f.getContentPane()
      val togPlay    = new JToggleButton( "Play" )
      val boxAmp     = Box.createHorizontalBox()
      val slidAmp    = new JSlider( 0, 0x10000 )
      boxAmp.add( new JLabel( "Amp:" ))
      boxAmp.add( slidAmp )
      val conAmp     = new SliderController( slidAmp.getModel(), p.amp, ParamSpec( 0.01, 1.0, ExpWarp ))
      val boxFreq    = Box.createHorizontalBox()
      val slidFreq   = new JSlider( 0, 0x10000 )
      boxFreq.add( new JLabel( "Freq:" ))
      boxFreq.add( slidFreq )
      val conFreq    = new SliderController( slidFreq.getModel(), p.freq, ParamSpec( 32, 18000, ExpWarp ))
      cp.setLayout( new BoxLayout( cp, BoxLayout.Y_AXIS ))
      cp.add( togPlay )
      cp.add( boxAmp )
      cp.add( boxFreq )

      ancestorAction( f.getRootPane() ) {
         csr.t { implicit c =>
            conAmp.register
            conFreq.register
         }
      } {
         sys.t { implicit c =>
            conAmp.unregister
            conFreq.unregister
         }
      }

      f.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE )
      f.pack()
      f.setLocationRelativeTo( null )
      f.setVisible( true )
      f
   }

   private case class SliderController( slid: BoundedRangeModel, con: Controller[ C, V ], conSpec: ParamSpec )
   extends Model.Listener[ C, Double ] with ChangeListener {
      me =>
      
      private val slidSpec = ParamSpec( slid.getMinimum(), slid.getMaximum() )

      private def updateView( force: Boolean )( implicit c: C ) {
         val v       = (slidSpec.map( conSpec.unmap( con.v.get )) + 0.5).toInt
         val changed = v != slid.getValue()
         if( changed ) {
            slid.removeChangeListener( me )
            slid.setValue( v )
         }
         if( changed || force ) slid.addChangeListener( me )
      }

      private def updateModel( implicit c: C ) {
         import de.sciss.synth._

         val v       = conSpec.map( slidSpec.unmap( slid.getValue() ))
         val changed = v.absdif( con.v.get ) > 1.0e-6  // like this?
         if( changed ) {
            con.removeListener( me )
            con.v.set( v )
            con.addListener( me )
         }
      }

      def register( implicit c: C ) {
         updateView( true )
         con.addListener( me )
      }

      def unregister( implicit c: CtxLike ) {
         con.removeListener( me )
         slid.removeChangeListener( me )
      }

      def stateChanged( e: ChangeEvent ) {
         csr.t { implicit c => updateModel }
      }

      def updated( v: Double )( implicit c: C ) {
         updateView( false )
      }
   }
}