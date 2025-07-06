# Advanced Java Repository of Niklas M. Gerbes

## Closing Project: Anatomy Explorer
![Milestone_2_Release.png](img/Milestone_2_Release.png)

### Behind the project: BodyParts3D Database
The Project is based on a Database for 3D anatomy visualizations by Fujieda et al.

>Mitsuhashi N, Fujieda K, Tamura T, Kawamoto S, Takagi T, Okubo K.
BodyParts3D: 3D structure database for anatomical concepts.
Nucleic Acids Res. 2008 Oct 3.
PMID: 18835852



### problems I encountered:
There is one big Problem overall: Connect both Trees (isA and partOf relations) that use fundamental different
concepts. The following properties made it hard to synchronize the selection over all three: both TreeViews and the visualization Pane:
- One FileID can be associated with multiple concepts WITHIN one tree 
  - A selection in the 3D pane can lead to multiple selection in either TreeView.
  - The most prominent example is "skin" and "integumentary system" from the partOf tree. Both concepts share the fileID FJ2810.
  If we interpret the fileIDs as SourceOfTruth for Our selection. Then selecting "skin" has to lead to a selection of "integumentary system".
  Or the other way around: selecting the mesh FJ2810 has to lead to the selection of "skin" and "integumentary system" in partOf and the associated Nodes in the isA tree.
- One Concept can have multiple FileIDs associated with it (Selecting a node in a TreeView can lead to a selection of multiple MeshView in the 3D pane).
- Concepts that have the same conceptID usually do not share the same Meshes (or fileIDs).
- Leaves of partOf and isA are not fully contained within each other (partOf has 869 leafes, isA has 1651 leafes and 770 leafes in total are common).

So it was quite some work to figure out how exactly to parse selections between the trees and visPane.

**This deep dive into the connections between FileIDs and ConceptIDs leads to the reason why it can happen that selecting one Node in 
a tree view might induce the selection of other nodes that share the selected FileIDs. I implemented it that way,
because I interpreted my selected Meshes (or FileIDs) as SourceOfTruth. Meaning selecting a Mesh leads to the selection of
ALL ASSOCIATED AnatomyNodes / treeItems.**