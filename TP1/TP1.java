//package TP1v3;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.LinkedList;

class TP1 {
	
	public static void main(String[] args) throws IOException {
		
		if(args.length != 1) {
			System.out.println("Erreur nombre d'arguments");
			System.exit(0);			
		}
		
		Graphe g = new Graphe(args[0]);
		
		if(g.nbBoucle > 0)
			System.out.println(g.nbBoucle + " boucles ont été ignorées");
		
		if(g.nbDoublonsAretes > 0)
			System.out.println(g.nbDoublonsAretes + " doublons ont été supprimés");
		
		System.out.println("Nombre de sommets: " + g.nbSommets);
		System.out.println("Nombres d'arêtes: " + g.nbAretes);
		System.out.println("Sommet de degré max (de numéro minimal): " + g.numSommetDegMax);
		
		System.out.println("Sa liste d'adjacence (ligne suivante):");
		g.mapSommets.get(g.numSommetDegMax).afficherListeNumAdjacence();
		
		System.out.println("Distribution des degrés: ");
		g.afficherDistributionDegres();
		
		System.out.println("Mémoire allouée : " +
				(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) + " octets");
		
	}
}

class Graphe {

	int nbSommets;						// nombre de sommets du graphe
	int nbAretes;						// nombre d'arêtes du graphe
	int nbDoublonsAretes;				// nombre de doublons d'aretes
	int nbBoucle;						// somme des boucles de tous les sommets

	int degMax;							// le degre maximum
	int numSommetDegMax;				// le sommet ayant le degré maximum du graphe
	
	HashMap<Integer,Sommet> mapSommets; // tableau dont les clés réprésentent les sommets, et les valeurs leurs liste d'adjacence:

	
	// fonction qui vérifie si un chaine de caractères représente bien est un entier:
	public static boolean estUnEntier(String chaine) {
		try {
			Integer.parseInt(chaine);
		} catch (NumberFormatException e){
			return false;
		}
 
		return true;
	}

	// fonction qui ajoute d'un nouveau sommet dans le graphe:
	public void addSommet(int s) {		
		if(!mapSommets.containsKey(s)) {
			mapSommets.put(s,new Sommet());
		}
	}

	// fonction qui fait en sorte s1 et s2 deviennent voisins: 
	// s1 est ajouté à la liste des voisins s2, et s2 est ajouté à la liste des voisins de s1:
	public void addAdjacent(int s1, int s2) {
		
		// cas d'une arete dont l'origine du sommet et la même que la destination:
		if(s1==s2) {
			nbBoucle++;
		}
		
		else {
			if(!mapSommets.get(s1).listNumAdjacence.contains(s2)) {
				mapSommets.get(s1).listNumAdjacence.add(s2);
				mapSommets.get(s2).listNumAdjacence.add(s1);
				nbAretes++;
			}else {
				nbDoublonsAretes++;
			}
		}	
		
	}

	// fonction qui ajoute une nouvelle arête dans le graphe à partir deux sommets 
	public void addArete(String sommet1, String sommet2, int numLigne) {
		
		// renvoie une erreur: si l'une des deux chaines ne représentent pas un entier dans une ligne
		if(!estUnEntier(sommet1) || !estUnEntier(sommet2)) {
			System.out.println("Erreur de format ligne " + numLigne);
			System.exit(0);
		}
		
		else {
			int s1 = Integer.parseInt(sommet1);
			int s2 = Integer.parseInt(sommet2);
	
			addSommet(s1);
			addSommet(s2);
	
			addAdjacent(s1,s2);
		}
		
	}

	public Graphe (String nomFichier) throws FileNotFoundException {
		
		mapSommets = new HashMap<Integer, Sommet>();

		Scanner sc;
		
		File f = new File(nomFichier);
		
		// cas ou le fichier n'existe pas
		if(!f.exists()) {
			System.out.println("Erreur entrée/sortie sur " + nomFichier);
			System.exit(0);			
		}
		
		sc = new Scanner(f);
		
		String[] tableau;
		String ligneSansTab;
		int numLigne=1;

		// parcours du fichier et ajout des arrêtes au graphe
		while(sc.hasNext()) {
			String ligne = sc.nextLine();
			if(ligne.charAt(0) == '#')
				continue;
			else {
				ligneSansTab = ligne.replace("\t", " ");
				tableau = ligneSansTab.split(" ");
				addArete(tableau[0], tableau[1], numLigne);
			}
			numLigne++;
		}

		sc.close();


		//trouver le sommet de plus grand numéro
		Set<Integer> set = mapSommets.keySet();
		@SuppressWarnings("rawtypes")
		TreeSet setTrie = new TreeSet<Integer>(set);
		int plusGrandNumero = (Integer)setTrie.last();
		
		//compléter les numéros manquants avec des sommets sans voisins
		for (int i = 0; i <= plusGrandNumero; i++) {
			if(!mapSommets.containsKey(i))
				mapSommets.put(i, new Sommet());
		}


		//calcul du sommet de degré max
		for (int key : mapSommets.keySet()) {
			if(mapSommets.get(key).listNumAdjacence.size() > degMax) {
				degMax = mapSommets.get(key).listNumAdjacence.size();
				numSommetDegMax = key;
			}
		}
		
		//compter le nombre de sommets
		nbSommets = mapSommets.size();

	}

	// fonction qui affiche tous les sommets du graphe
	public void afficher() {
		for (int key : mapSommets.keySet()) {
			System.out.println(mapSommets.get(key));
		}
	}

	// fonction qui affiche la distribution de degrés du graphe:
	public void afficherDistributionDegres() {
		
		// initialement, dans le tableau de degrés tabDeg: il y a 0 sommets de degré 0, 0 sommets de degré 1, 0 sommets de degré 2...
		int[] tabDeg = new int[degMax+1];
		
		// parcours de chaque sommet du graphe: 
		// pour chaque sommet, on calcule leur degré à partir de leur liste d'adjacence
		// A partir de ce degré, on incrémente la valeur de clé "degré" dans le tableau de degrés tabDeg
		for (int key : mapSommets.keySet()) {
			int deg = mapSommets.get(key).nbAdjacent();
			tabDeg[deg]++;
		}

		// affichage du tableau de degrés:
		for (int i = 0; i < tabDeg.length; i++) 
			System.out.println(i + " " + tabDeg[i]);

	}


}

class Sommet {
	
	int degre;								// nombre de voisins d'un sommet
	LinkedList<Integer> listNumAdjacence;	// représente les voisins d'un sommet
	
	
	public Sommet() {
		listNumAdjacence = new LinkedList<Integer>();
	}
	
	
	// fonction qui affiche la liste d'adjacence d'un sommet:
	public void afficherListeNumAdjacence() {
		for (int i = 0; i < listNumAdjacence.size(); i++) {
			System.out.print(listNumAdjacence.get(i) + " ");
		}
		System.out.println();
	}
	
	
	// fonction qui renvoie le nombre de voisins d'un sommet:
	public int nbAdjacent() {
		return listNumAdjacence.size();
	}
	
	
}