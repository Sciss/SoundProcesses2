/*
 *  Model.scala
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

import collection.immutable.{Queue => IQueue}
import concurrent.stm.{Txn, TxnLocal}

object Model {
   trait Listener[ -C, -T ] {
      def updated( v: T )( implicit c: C ) : Unit
   }

   def onCommit[ C <: Ct, T ]( committed: Traversable[ T ] => Unit ) : Listener[ C, T ] =
      filterOnCommit( (_: T, _: C) => true )( committed )

   def filterOnCommit[ C <: Ct, T ]( filter: Function2[ T, C, Boolean ])( committed: Traversable[ T ] => Unit ) =
      new Listener[ C, T ] {
         val queueRef = TxnLocal[ IQueue[ T ]]( init = IQueue.empty )

         def updated( update: T )( implicit c: C ) {
            if( filter( update, c )) {
               val txn  = c.txn
               val q0   = queueRef.get( txn )
               queueRef.set( q0 enqueue update )( txn )
               if( q0.isEmpty ) {
                  Txn.beforeCommit( txn => {
                     val q1 = queueRef.get( txn )
                     Txn.afterCommit( _ => committed( q1 ))( txn )
                  })( txn ) // , Int.MaxValue
               }
            }
         }
      }

   def reduceOnCommit[ C <: Ct, T, U ]( pf: PartialFunction[ (T, C), U ])( committed: U => Unit ) =
      new Listener[ C, T ] {
         val lifted = pf.lift
         val queueRef = TxnLocal[ U ]( init = null.asInstanceOf[ U ])

         def updated( update: T )( implicit c: C ) {
            lifted( update -> c ).foreach { u =>
               val txn  = c.txn
               val q0   = queueRef.get( txn )
               queueRef.set( u )( txn )
               if( q0 == null ) {
                  Txn.beforeCommit( txn => {
                     val q1 = queueRef.get( txn )
                     Txn.afterCommit( _ => committed( q1 ))( txn )
                  })( txn ) // , Int.MaxValue
               }
            }
         }
      }
}

trait Model[ C, +T ] {
   import Model._

   type L = Listener[ C, T ]

   def addListener( l: Listener[ C, T ])( implicit c: CtxLike ) : Unit
   def removeListener( l: Listener[ C, T ])( implicit c: CtxLike ) : Unit
}
