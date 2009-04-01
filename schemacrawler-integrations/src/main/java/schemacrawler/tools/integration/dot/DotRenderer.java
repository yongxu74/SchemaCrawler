/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2009, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.tools.integration.dot;


import java.awt.Color;
import java.io.Writer;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnMap;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;
import schemacrawler.tools.integration.SchemaRenderer;
import schemacrawler.tools.util.HtmlFormattingHelper;
import sf.util.Utilities;

/**
 * Main executor for the dot engine integration.
 * 
 * @author Sualeh Fatehi
 */
public final class DotRenderer
  extends SchemaRenderer
{

  public static final String NEWLINE = System.getProperty("line.separator");

  private static String dotHeader(final String name)
  {
    final byte[] text = Utilities.readFully(HtmlFormattingHelper.class
      .getResourceAsStream("/dot.header.txt"));
    final String dotHeader = new String(text);
    return String.format(dotHeader, name);
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.integration.TemplatedSchemaRenderer#render(java.lang.String,
   *      schemacrawler.schema.Schema, java.io.Writer)
   */
  @Override
  protected void render(final String configFileName,
                        final Catalog catalog,
                        final Writer writer)
    throws Exception
  {
    if (catalog == null || writer == null)
    {
      return;
    }

    writer.write(dotHeader(catalog.getName()));
    for (final Schema schema: catalog.getSchemas())
    {
      final Color bgcolor = newPastel();
      for (final Table table: schema.getTables())
      {
        final StringBuilder buffer = new StringBuilder();
        final String tableName = table.getFullName();
        buffer.append("  \"" + tableName + "\" [").append(NEWLINE)
          .append("    label=<").append(NEWLINE);
        buffer
          .append("      <table border=\"1\" cellborder=\"0\" cellspacing=\"0\">")
          .append(NEWLINE);
        buffer.append("        <tr>").append(NEWLINE);
        buffer.append("          <td colspan=\"2\" bgcolor=\""
                      + htmlColor(bgcolor.darker()) + "\" align=\"left\">"
                      + tableName + "</td>").append(NEWLINE);
        buffer.append("          <td bgcolor=\"" + htmlColor(bgcolor.darker())
                      + "\" align=\"right\">"
                      + ((table instanceof View)? "[view]": "[table]")
                      + "</td>").append(NEWLINE);
        buffer.append("        </tr>").append(NEWLINE);
        for (final Column column: table.getColumns())
        {
          final String columnName = column.getName();
          final Color columnBgcolor;
          if (column.isPartOfPrimaryKey())
          {
            columnBgcolor = bgcolor;
          }
          else
          {
            columnBgcolor = Color.white;
          }
          buffer.append("        <tr>").append(NEWLINE);
          buffer.append("          <td port=\"" + columnName
                        + ".start\" bgcolor=\"" + htmlColor(columnBgcolor)
                        + "\" align=\"left\">" + columnName + "</td>")
            .append(NEWLINE);
          buffer.append("          <td bgcolor=\"" + htmlColor(columnBgcolor)
                        + "\"> </td>").append(NEWLINE);
          buffer.append("          <td port=\"" + columnName
                        + ".end\" align=\"right\" bgcolor=\""
                        + htmlColor(columnBgcolor) + "\">"
                        + column.getType().getDatabaseSpecificTypeName()
                        + column.getWidth() + "</td>").append(NEWLINE);
          buffer.append("        </tr>").append(NEWLINE);
        }
        buffer.append("      </table>").append(NEWLINE).append("    >")
          .append(NEWLINE).append("  ];").append(NEWLINE);

        for (final ForeignKey foreignKey: table.getForeignKeys())
        {
          for (final ForeignKeyColumnMap foreignKeyColumnMap: foreignKey
            .getColumnPairs())
          {
            final Column primaryKeyColumn = foreignKeyColumnMap
              .getPrimaryKeyColumn();
            final Column foreignKeyColumn = foreignKeyColumnMap
              .getForeignKeyColumn();
            if (primaryKeyColumn.getParent().equals(table))
            {
              final String arrowhead;
              if (foreignKeyColumn.isNullable())
              {
                arrowhead = "odottee";
              }
              else
              {
                arrowhead = "teetee";
              }
              final String arrowtail;
              if (foreignKeyColumn.isPartOfUniqueIndex())
              {
                arrowtail = "teeodot";
              }
              else
              {
                arrowtail = "crowodot";
              }
              buffer
                .append(String
                  .format("  \"%s\":\"%s.start\":w -> \"%s\":\"%s.end\":e [label=%s arrowhead=%s arrowtail=%s];%n",
                          primaryKeyColumn.getParent().getFullName(),
                          primaryKeyColumn.getName(),
                          foreignKeyColumn.getParent().getFullName(),
                          foreignKeyColumn.getName(),
                          foreignKey.getName(),
                          arrowhead,
                          arrowtail));
            }
          }
        }

        buffer.append(NEWLINE).append(NEWLINE);
        writer.write(buffer.toString());
      }
    }
    writer.write("}\n");
    writer.flush();
    writer.close();
  }

  private int colorValue()
  {
    final int colorBase = 200;
    final int colorValue = (int) (Math.random() * (255D - colorBase) + colorBase);
    return colorValue;
  }

  private String htmlColor(final Color color)
  {
    return "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
  }

  private Color newPastel()
  {
    return new Color(colorValue(), colorValue(), colorValue());
  }

}
