/*
 *  OfflineVisualView.scala
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
package view

import GUIUtils._
import javax.swing.{BorderFactory, JLabel, JComponent, JViewport, Box, JPanel, ScrollPaneConstants => SPC, JScrollPane, JFrame}
import javax.swing.border.BevelBorder
import java.awt.{Cursor => AWTCursor, _}

class OfflineVisualView[ C <: Ct, V[ ~ ] <: Vr[ C, ~ ]]( sys: System[ C, V ],
                                                         g: ProcGroup[ C, V ], csr: Cursor[ C, V ]) {
   private var viewSpan          = Interval( Period( 0.0 ), Period( 60.0 ))
   private var map               = Map.empty[ Proc[ C, V ], ProcView ]
   private val columnHeaderView  = Box.createVerticalBox()
   private val rowHeaderView     = new JPanel( new GridLayout( 0, 1 ))
   private val tracksView        = new JPanel( new GridLayout( 0, 1 ))

   val (frame, axis) = {
      val l = Model.filterOnCommit[ C, ProcGroup.Update[ C, V ]]( (_, c) => csr.isApplicable( c ))( tr =>
         defer( tr.foreach {
            case ProcGroup.ProcAdded( p )   => add( p )
            case ProcGroup.ProcRemoved( p ) => remove( p )
         }))

      val f                = new JFrame( "Offline View (" + g.name + ")" )
      val cp               = f.getContentPane()
      val timelineAxis     = new Axis( Axis.HORIZONTAL, Axis.TIMEFORMAT )
      timelineAxis.minimum = viewSpan.start.v
      timelineAxis.maximum = viewSpan.end.v
//      timelineAxis.setPreferredSize( new Dimension( 40, 40 ))
      val tracksPanel      = new JScrollPane( SPC.VERTICAL_SCROLLBAR_ALWAYS, SPC.HORIZONTAL_SCROLLBAR_ALWAYS )
      tracksPanel.setBorder( null )
val viewPort = new JViewport()
      viewPort.setBackground( new Color( 0x28, 0x28, 0x28 ))
      tracksPanel.setViewport( viewPort )
//      viewPort.setBorder( null )
      tracksPanel.setCorner( SPC.UPPER_LEFT_CORNER, new JPanel() ) // fixes white background problem
      tracksPanel.setCorner( SPC.LOWER_LEFT_CORNER, new JPanel() ) // fixes white background problem
      tracksPanel.setCorner( SPC.UPPER_RIGHT_CORNER, new JPanel() ) // fixes white background problem
//val timelinePanel = new JPanel()
      tracksPanel.setViewportView( tracksView )
      tracksPanel.setRowHeaderView( rowHeaderView )
      columnHeaderView.add( timelineAxis )
      tracksPanel.setColumnHeaderView( columnHeaderView )
      timelineAxis.viewPort = Some( tracksPanel.getColumnHeader )
      cp.add( tracksPanel, BorderLayout.CENTER )

      ancestorAction( tracksPanel ) {
         csr.t { implicit c =>
            addFull( g.all )
            g.addListener( l )( c.eph )
         }
      } {
         sys.t { implicit c => g.removeListener( l )}
      }

      f.setSize( 600, 300 )
      f.setLocationRelativeTo( null )
      f.setVisible( true )
      (f, timelineAxis)
   }

   private def addFull( ps: Traversable[ Proc[ C, V ]]) {
      rowHeaderView.removeAll()
      ps.foreach( add( _ ))
   }

   private def add( p: Proc[ C, V ]) {
//      listModel.addElement( p )
      val c = new JLabel( p.name )
      c.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ))
      c.setPreferredSize( new Dimension( 48, 64 ))
      rowHeaderView.add( c )
      rowHeaderView.revalidate()
      val t = new TrackView( p )
      t.setPreferredSize( new Dimension( 480, 64 ))
      tracksView.add( t )
      tracksView.revalidate()
      map += p -> ProcView( c, t )
   }

   private def remove( p: Proc[ C, V ]) {
//      listModel.removeElement( p )
      val pvo = map.get( p )
      map -= p
      pvo.foreach { pv =>
         rowHeaderView.remove( pv.header )
         rowHeaderView.revalidate()
         tracksView.remove( pv.trackView )
         tracksView.revalidate()
      }
   }

   private class TrackView( p: Proc[ C, V ]) extends JComponent {
      setBorder( BorderFactory.createMatteBorder( 1, 0, 1, 0, Color.white ))
      setOpaque( true )

      override def paintComponent( g: Graphics ) {
         val in = getInsets()
         val g2 = g.asInstanceOf[ Graphics2D ]
         val x = in.left
         val y = in.top
         val w = getWidth() - (in.left + in.right)
         val h = getHeight() - (in.top + in.bottom)
//         g2.setColor( Color.blue )
//         g2.fillRect( x, y, w, h )

//         p.amp.range...
      }
   }

   private case class ProcView( header: JComponent, trackView: JComponent )
}