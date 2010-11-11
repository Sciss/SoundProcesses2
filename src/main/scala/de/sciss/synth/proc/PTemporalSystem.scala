/*
 *  PTemporalSystem.scala
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

import edu.stanford.ppl.ccstm.{TxnLocal, STM, Txn, Ref}
import collection.immutable.{SortedMap => ISortedMap}
import Double.{PositiveInfinity => dinf}

object PTemporalSystem extends System[ PTemporal ] {
   private type C = Ctx[ PTemporal ]

   def t[ T ]( fun: C => T ) : T = STM.atomic( tx => fun( new PTemporal( tx )))

   def at[ T ]( period: Period )( thunk: => T )( implicit c: C ) : T =
      during( Interval( period, Period( dinf )))( thunk )

   def during[ T ]( interval: Interval )( thunk: => T )( implicit c: C ) : T = {
      val oldIval = c.repr.interval
      c.repr.interval = interval
      try { thunk } finally { c.repr.interval = oldIval }
   }
}

class PTemporal private[proc]( private[proc] val txn: Txn )
extends Ctx[ PTemporal ] {

   private type C = Ctx[ PTemporal ]

   private val intervalRef = new TxnLocal[ Interval ] {
      override protected def initialValue( txn: Txn ) = Interval( Period( 0.0 ), Period( dinf ))
   }

   def repr = this
   def system = PTemporalSystem

   def v[ T ]( init: T )( implicit m: ClassManifest[ T ]) : Var[ PTemporal, T ] =
      new PVar( Ref( ISortedMap( Period( 0.0 ) -> init )( Ordering.ordered[ Period ])))


   def period : Period     = interval.start
   def interval : Interval = intervalRef.get( txn )

   private[proc] def interval_=( newInterval: Interval ) = intervalRef.set( newInterval )( txn )

   private class PVar[ /* @specialized */ T ]( ref: Ref[ ISortedMap[ Period, T ]]) extends Var[ PTemporal, T ] {
      def get( implicit c: C ) : T = {
         val map = ref.get( c.repr.txn )
         map.to( c.repr.period ).last._2  // XXX is .last efficient? we might need to switch to FingerTree.Ranged
      }
      def set( v: T )( implicit c: C ) {
         val ival = c.repr.interval
         ref.transform( map =>
            map.to( ival.start ) +
            (ival.start -> v) +
            (ival.end -> map.to( ival.end ).last._2) ++ // XXX .last efficient?
            map.from( ival.end )
         )( c.repr.txn )
      }
   }
}