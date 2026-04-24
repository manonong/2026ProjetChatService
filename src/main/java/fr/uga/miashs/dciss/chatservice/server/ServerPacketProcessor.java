/*
 * Copyright (c) 2024.  Jerome David. Univ. Grenoble Alpes.
 * This file is part of DcissChatService.
 *
 * DcissChatService is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * DcissChatService is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.uga.miashs.dciss.chatservice.server;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import fr.uga.miashs.dciss.chatservice.common.Packet;

public class ServerPacketProcessor implements PacketProcessor {
    private final static Logger LOG = Logger.getLogger(ServerPacketProcessor.class.getName());
    private ServerMsg server;


    public ServerPacketProcessor(ServerMsg s) {
        this.server = s;
    }
	


	@Override
	public void process(Packet p) {
		// ByteBufferVersion. On aurait pu utiliser un ByteArrayInputStream + DataInputStream à la place
		ByteBuffer buf = ByteBuffer.wrap(p.data);
		byte type = buf.get();
		
		if (type == 1) { // cas creation de groupe
			createGroup(p.srcId,buf);
		} else {
			LOG.warning("Server message of type=" + type + " not handled by procesor");
		}
		
        if (type == 2) { // suppression groupe
            leaveGroup(p.srcId, buf);

        }else if (type == 3) { // ajouter un user dans un groupe
            int groupId = buf.getInt();
            int userId = buf.getInt();

            GroupMsg g = server.getGroup(groupId);
            UserMsg u = server.getUser(userId);

            if (g != null && u != null) {
                g.addMember(u);
            } else {
        LOG.warning("Add member failed: group or user not found");
            }
        }

        else if (type == 4) { // retirer un user d'un groupe
            int groupId = buf.getInt();
            int userId = buf.getInt();

            GroupMsg g = server.getGroup(groupId);
            UserMsg u = server.getUser(userId);

            if (g != null && u != null) {
                g.removeMember(u);
            } else {
                LOG.warning("Remove member failed: group or user not found");
            }
        } 
       
        else if (type == 5) { // changer le nom d'un groupe
            int groupId = buf.getInt();

            int length = buf.getInt(); // longueur du nom
            byte[] nameBytes = new byte[length];
            buf.get(nameBytes);
            String newName = new String(nameBytes);

            GroupMsg g = server.getGroup(groupId);

            if (g != null) {
                // vérifie que l'expéditeur est le propriétaire
                if (g.getOwner().getId() == p.srcId) {
                    g.setName(newName);
                } else {
                    LOG.warning("Rename refused: user is not owner");
                }
            } else {
                LOG.warning("Rename failed: group not found");
            }
        }
       
        else if (type == 6) { // transferer la propriété d'un groupe
            int groupId = buf.getInt();
            int newOwnerId = buf.getInt();

            GroupMsg g = server.getGroup(groupId);
            UserMsg newOwner = server.getUser(newOwnerId);

            if (g != null && newOwner != null) {

                // vérifie que l'expéditeur est le propriétaire actuel
                if (g.getOwner().getId() == p.srcId) {

                    boolean ok = g.changeOwner(newOwner);

                    if (!ok) {
                        LOG.warning("Transfer failed: new owner not in group");
                    }

                } else {
                    LOG.warning("Transfer refused: user is not owner");
                }

            } else {
                LOG.warning("Transfer failed: group or user not found");
            }
        }  


        else if (type == 7) { // supprimer un groupe
            int groupId = buf.getInt();
            server.removeGroup(groupId);
        }


        else if (type == 8) { // modifier son username
            int length = buf.getInt();
            byte[] nameBytes = new byte[length];
            buf.get(nameBytes);
            String newUsername = new String(nameBytes);

            UserMsg u = server.getUser(p.srcId);

            if (u != null) {
                u.setUsername(newUsername);
            } else {
                LOG.warning("Username change failed: user not found");
            }
        }
    }
	
	public void createGroup(int ownerId, ByteBuffer data) {
		int nb = data.getInt();
		GroupMsg g = server.createGroup(ownerId);
		for (int i = 0; i < nb; i++) {
			int userId = data.getInt();
			UserMsg u = server.getUser(userId);

			if (u != null) {
				boolean added = g.addMember(u);
				if (added) {
					server.getDb().insertMember(g.getId(), u.getId());
				}
			} else {
				System.out.println("USER " + userId + " NOT FOUND");
			}
		}
	}


    public void leaveGroup(int userId, ByteBuffer data) { //data est l'id du groupe à quitter
        int groupId = data.getInt();
        GroupMsg g = server.getGroup(groupId);
        if (g != null) { // si le groupe id existe fait
            g.removeMember(server.getUser(userId));
        }
    }
}


