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
        }


        if (type == 3){//ajouter un user dans un groupe
			addUserGroup(p.srcId, buf);

        }


        if (type == 4){//retirer un user dans un groupe
			deleteUserGroup(p.srcId, buf);
        }  
       
        if (type == 5){//changer le nom d'un groupe


        }  
       
        if (type == 6){//transferer la propriété d'un groupe
			transferOwnership(p.srcId, buf);

        }  


        if (type == 7){//supprimer un groupe


        }


        if (type == 8){//modifier son username


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

	public void addUserGroup(int userId, ByteBuffer data) {
		int groupId = data.getInt();// lit positions 1-4, curseur passe à 5
		int addedUserId = data.getInt();// lit positions 5-8, curseur passe à 9

		GroupMsg g = server.getGroup(groupId);
	    if (g == null) return;

		//////vérifie si qd t'ajoutes un membre il faut que tu sois owner
		g.addMemberIfOwner(userId, server.getUser(addedUserId));
	}

	public void deleteUserGroup(int userId, ByteBuffer data){
		int groupId = data.getInt();// lit positions 1-4, curseur passe à 5
		int deleteUserId = data.getInt();// lit positions 5-8, curseur passe à 9

		GroupMsg g = server.getGroup(groupId);
		if (g==null) return; //groupe existe pas

		g.deleteMemberIfOwner(userId, server.getUser(deleteUserId)); //vérifie si admin
	}	

	public void transferOwnership(int userId, ByteBuffer data){
		int groupId = data.getInt();// lit positions 1-4, curseur passe à 5
		int newOwnerId = data.getInt();// lit positions 5-8, curseur passe à 9	
		
		GroupMsg g = server.getGroup(groupId);
		if (g==null) return; //groupe existe pas

    	g.transferOwnerIfOwner(userId, server.getUser(newOwnerId));
	}


}


