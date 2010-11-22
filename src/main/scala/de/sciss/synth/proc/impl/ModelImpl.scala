package de.sciss.synth.proc.impl

import edu.stanford.ppl.ccstm.Ref
import collection.immutable.{Queue => IQueue}
import de.sciss.synth.proc.{ECtx, Model}

trait ModelImpl[ C, T ] extends Model[ C, T ] {
   import Model._

   private val listeners = Ref( IQueue.empty[ L ])

   def addListener[ X[ _ ]]( l: L )( implicit c: ECtx ) {
      listeners.transform( _ enqueue l )( c.txn )
   }

   def removeListener[ X[ _ ]]( l: L )( implicit c: ECtx ) {
      listeners.transform( _.filter( _ != l ))( c.txn )
   }

   protected def fireUpdate( u: T )( implicit c: C ) {
      listeners.get( c.txn ).foreach( _.updated( u )( c ))
   }
}
