/*
 *  KTemporalSystem.scala
 *  (SoundProcesses)
 *
 *  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.synth.proc

import edu.stanford.ppl.ccstm.{STM, Txn, Ref}
import de.sciss.confluent._

object KTemporalSystem extends System[ KTemporalCtx ] {
   private type C = Ctx[ KTemporalCtx ]

   private val pathRef = Ref( VersionPath.init )

   def t[ T ]( fun: C => T ) : T = STM.atomic( tx => fun( new KTemporalCtx( tx )))

 }

class KTemporalCtx private[proc]( private[proc] val txn: Txn )
extends Ctx[ KTemporalCtx ] {
   private type C = Ctx[ KTemporalCtx ]

   def repr = this
   def system = KTemporalSystem

   def v[ T ]( init: T )( implicit m: ClassManifest[ T ]) : Var[ KTemporalCtx, T ] = {
//      val ref = Ref[ T ]()
      new KVar( Ref( init ))
   }

   private class KVar[ /* @specialized */ T ]( ref: Ref[ T ]) extends Var[ KTemporalCtx, T ] {
       def get( implicit c: C ) : T = ref.get( c.repr.txn )
       def set( v: T )( implicit c: C ) : Unit = ref.set( v )( c.repr.txn )
    }
}