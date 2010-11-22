/*
 *  ESystemImpl.scala
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
package impl

import edu.stanford.ppl.ccstm.{Ref, Txn, STM}

object ESystemImpl extends ESystem {
   override def toString = "ESystem"

   def t[ T ]( fun: ECtx => T ) : T = STM.atomic( tx => fun( new Ctx( tx )))

   def v[ T ]( init: T )( implicit m: ClassManifest[ T ], c: ECtx ) : EVar[ ECtx, T ] =
      new Var( Ref( init ), m.toString )

   def modelVar[ T ]( init: T )( implicit m: ClassManifest[ T ], c: ECtx ) : EVar[ ECtx, T ] with Model[ ECtx, T ] =
      new ModelVar( Ref( init ), m.toString )

   def userVar[ T ]( init: T )( user: (ECtx, T) => Unit )( implicit m: ClassManifest[ T ], c: ECtx ) : EVar[ ECtx, T ] =
      new UserVar( Ref( init ), m.toString, user )

   def join( txn: Txn ) : ECtx = new Ctx( txn )

   private class Ctx( val txn: Txn ) extends ECtx {
      override def toString = "ECtx"
      def eph : ECtx = this
   }

   private trait AbstractVar[ T ] extends EVar[ ECtx, T ] {
      protected val ref: Ref[ T ]
      protected val typeName: String

      protected def fireUpdate( v: T )( implicit c: ECtx ) : Unit

      override def toString = "EVar[" + typeName + "]"

      def get( implicit c: ECtx ) : T = ref.get( c.txn )
      def set( v: T )( implicit c: ECtx ) {
         ref.set( v )( c.txn )
         fireUpdate( v )
      }
   }

   private class Var[ T ]( val ref: Ref[ T ], val typeName: String ) extends AbstractVar[ T ] {
      protected def fireUpdate( v: T )( implicit c: ECtx ) {}
   }

   private class ModelVar[ T ]( val ref: Ref[ T ], val typeName: String )
   extends AbstractVar[ T ] with ModelImpl[ ECtx, T ]

   private class UserVar[ T ]( val ref: Ref[ T ], val typeName: String, user: (ECtx, T) => Unit )
   extends AbstractVar[ T ] {
      protected def fireUpdate( v: T )( implicit c: ECtx ) { user( c, v )}
   }
}