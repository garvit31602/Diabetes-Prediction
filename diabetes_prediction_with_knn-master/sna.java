final float intraCommunityP = (float) (0.5 + Math.random() * 0.25);  // Random value between 0.0 and 0.5
final float interCommunityP = (float) (Math.random() * 0.25);  // Random value between 0.5 and 1.0
int elapsedTime = 0; // Elapsed time in seconds
int timeStep = 100; // Time step in milliseconds
int infectedCount = 0; // Count of infected nodes
float infectionPercentage = 0; // Infection percentage
int initialPopulation = 100; // Number of nodes
boolean simulationActive = true; // Stops when all edges are processed

ArrayList<Node> nodes; // List of nodes (people)
ArrayList<Edge> edges; // List of edges (connections)
ArrayList<ArrayList<Node>> communities; // Communities of nodes
ArrayList<Integer> infectionHistory;
ArrayList<Integer> timeHistory;

int currentEdgeIndex = 0; // Tracks the current edge being processed
ArrayList<Edge> processedEdges = new ArrayList<Edge>(); // To track processed edges
ArrayList<Edge> edgesToProcess = new ArrayList<Edge>(); // List of edges to be processed
ArrayList<Node> infectedNodes = new ArrayList<Node>(); // List of infected nodes

void setup() {
  fullScreen();
  textSize(20);
  nodes = new ArrayList<Node>();
  edges = new ArrayList<Edge>();
  infectionHistory = new ArrayList<Integer>();
  timeHistory = new ArrayList<Integer>();
  background(30);
  frameRate(60);

  // Initialize network
  initializeNetwork();

  // Perform modularity-based community detection
  detectCommunities();

  // Set the first infected node
  if (!nodes.isEmpty()) {
    int initialInfectedIndex = (int) random(nodes.size());
    Node initialInfectedNode = nodes.get(initialInfectedIndex);
    initialInfectedNode.isInfected = true;
    infectedCount = 1;
    infectedNodes.add(initialInfectedNode); // Add the initially infected node

    // Add all edges of the initially infected node to the edgesToProcess list
    addEdgesToProcess(initialInfectedNode);
  }
}

void draw() {
  if (!simulationActive) {
    noLoop(); // Stop simulation
    return;
  }

  background(30, 30, 30, 20);

  // Update simulation time
  elapsedTime += timeStep / 1000;
  trackInfectionHistory();

  // Display statistics and visual elements
  displayStatistics();
  displayCommunityChart();
  displayInfectionGraph();

  // Process one edge per frame
  if (edgesToProcess.size() > 0) {
    processCurrentEdge();
  }

  // Update and display all nodes
  for (Node n : nodes) {
    n.display();
  }

  // Update and display all edges
  for (Edge e : edges) {
    e.display();
  }

  // Stop simulation if all edges are processed
  if (currentEdgeIndex >= edges.size() && edgesToProcess.size() == 0) {
    simulationActive = false;
  }
}

void processCurrentEdge() {
  if (edgesToProcess.size() == 0) return;

  // Get the next edge to process
  Edge e = edgesToProcess.remove(0); // Remove and get the first edge
  
  // If this edge has already been processed, skip it
  if (processedEdges.contains(e)) return;

  Node a = e.a;
  Node b = e.b;
  boolean spread = false;

  // Check if either of the nodes is infected and process infection spread
  if (a.isInfected && !b.isInfected) {
    float proba = (a.communityId == b.communityId) ? intraCommunityP : interCommunityP;
    if (random(1) < proba) {
      b.isInfected = true;
      infectedCount++;
      infectedNodes.add(b); // Add to the list of infected nodes
      spread = true;
      // Add all edges connected to the newly infected node
      addEdgesToProcess(b);
    }
  } else if (b.isInfected && !a.isInfected) {
    float proba = (b.communityId == a.communityId) ? intraCommunityP : interCommunityP;
    if (random(1) < proba) {
      a.isInfected = true;
      infectedCount++;
      infectedNodes.add(a); // Add to the list of infected nodes
      spread = true;
      // Add all edges connected to the newly infected node
      addEdgesToProcess(a);
    }
  }

  // Update edge state
  e.isRed = spread;
  e.isGreen = !spread;

  // Mark this edge as processed
  processedEdges.add(e);

  // Advance to next edge
  currentEdgeIndex++;
  infectionPercentage = (infectedCount / (float) nodes.size()) * 100;
}

void addEdgesToProcess(Node n) {
  // Add all edges connected to node n to the processing queue
  for (Edge e : edges) {
    if ((e.a == n || e.b == n) && !processedEdges.contains(e)) {
      edgesToProcess.add(e);
    }
  }
}

void trackInfectionHistory() {
  infectionHistory.add(infectedCount);
  timeHistory.add(elapsedTime);
}

void displayStatistics() {
  fill(50, 50, 50, 200);
  noStroke();
  rect(10, 10, 300, 150);

  fill(255);
  text("People infected: " + infectedCount, 20, 40);
  text("Time: " + elapsedTime + "s", 20, 70);
  text("Infection %: " + nf(infectionPercentage, 0, 2) + "%", 20, 100);
  text("Edges Processed: " + currentEdgeIndex + "/" + edges.size(), 20, 130);
}

void displayCommunityChart() {
  int chartY = 200;
  fill(50, 50, 50, 200);
  rect(width - 290, 10, 280, height - 20);

  fill(255);
  text("Community Sizes", width - 280, chartY - 170);

  for (int i = 0; i < communities.size(); i++) {
    ArrayList<Node> community = communities.get(i);
    color c = community.get(0).communityColor;

    fill(c);
    rect(width - 270, chartY + (i * 30), 20, 20);

    fill(255);
    text("Size: " + community.size(), width - 240, chartY + (i * 30) + 15);
  }
}

void displayInfectionGraph() {
  stroke(255);
  fill(50, 50, 50, 200);
  rect(width - 300, height - 200, 290, 190);

  fill(255);
  text("Infection History", width - 290, height - 180);

  if (timeHistory.size() > 1) {
    // Convert ArrayList<Integer> to int[]
    int[] infectionArray = new int[infectionHistory.size()];
    for (int i = 0; i < infectionHistory.size(); i++) {
      infectionArray[i] = infectionHistory.get(i);
    }

    int maxInfected = max(infectionArray);  // Now using max() on the array
    for (int i = 1; i < timeHistory.size(); i++) {
      float y1 = map(infectionHistory.get(i - 1), 0, maxInfected, height - 20, height - 190);
      float y2 = map(infectionHistory.get(i), 0, maxInfected, height - 20, height - 190);
      float x1 = map(timeHistory.get(i - 1), 0, timeHistory.get(timeHistory.size() - 1), width - 290, width - 10);
      float x2 = map(timeHistory.get(i), 0, timeHistory.get(timeHistory.size() - 1), width - 290, width - 10);

      stroke(255, 0, 0);
      line(x1, y1, x2, y2);
    }
  }
}


// Initialize the network with nodes and edges
void initializeNetwork() {
  // Create nodes
  for (int i = 0; i < initialPopulation; i++) {
    float x = random(width - 300); // Avoid overlapping with side chart
    float y = random(height);
    nodes.add(new Node(x, y));
  }

  // Create edges (randomly connect nodes)
  for (int i = 0; i < nodes.size(); i++) {
    for (int j = i + 1; j < nodes.size(); j++) {
      if (random(1) < 0.1) { // 10% chance to connect nodes
        edges.add(new Edge(nodes.get(i), nodes.get(j)));
      }
    }
  }
}

// Perform modularity-based community detection
void detectCommunities() {
  int n = nodes.size();
  int[][] adjacencyMatrix = new int[n][n];

  // Populate adjacency matrix from edges
  for (Edge e : edges) {
    int indexA = nodes.indexOf(e.a);
    int indexB = nodes.indexOf(e.b);
    adjacencyMatrix[indexA][indexB] = 1;
    adjacencyMatrix[indexB][indexA] = 1;
  }

  communities = modularityClustering(adjacencyMatrix);

  // Assign community ID and color to each node
  for (int i = 0; i < communities.size(); i++) {
    ArrayList<Node> community = communities.get(i);
    color communityColor = color(random(255), random(255), random(255));
    for (Node nn : community) {
      nn.communityId = i;
      nn.communityColor = communityColor;
    }
  }
}

// Modularity-based clustering
ArrayList<ArrayList<Node>> modularityClustering(int[][] adjacencyMatrix) {
  int n = adjacencyMatrix.length;

  // Initially, each node is in its own community
  ArrayList<ArrayList<Integer>> communityIndices = new ArrayList<ArrayList<Integer>>();
  for (int i = 0; i < n; i++) {
    ArrayList<Integer> singleNodeCommunity = new ArrayList<Integer>();
    singleNodeCommunity.add(i);
    communityIndices.add(singleNodeCommunity);
  }

  // Compute the degree of each node
  int[] degree = new int[n];
  int totalEdges = 0;
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
      if (adjacencyMatrix[i][j] == 1) {
        degree[i]++;
        totalEdges++;
      }
    }
  }
  totalEdges /= 2; // Because the graph is undirected

  // Modularity optimization loop
  boolean improvement = true;
  while (improvement) {
    improvement = false;
    for (int i = 0; i < communityIndices.size(); i++) {
      for (int j = i + 1; j < communityIndices.size(); j++) {
        // Compute modularity gain if we merge communities i and j
        float modularityBefore = computeModularity(communityIndices, adjacencyMatrix, degree, totalEdges);
        ArrayList<Integer> mergedCommunity = new ArrayList<Integer>(communityIndices.get(i));
        mergedCommunity.addAll(communityIndices.get(j));

        ArrayList<ArrayList<Integer>> newCommunities = new ArrayList<ArrayList<Integer>>(communityIndices);
        newCommunities.remove(j);
        newCommunities.remove(i);
        newCommunities.add(mergedCommunity);

        float modularityAfter = computeModularity(newCommunities, adjacencyMatrix, degree, totalEdges);

        // If merging increases modularity, merge the communities
        if (modularityAfter > modularityBefore) {
          communityIndices = newCommunities;
          improvement = true;
          break;
        }
      }
      if (improvement) break;
    }
  }

  // Convert indices back to node objects
  ArrayList<ArrayList<Node>> resultCommunities = new ArrayList<ArrayList<Node>>();
  for (ArrayList<Integer> community : communityIndices) {
    ArrayList<Node> nodeCommunity = new ArrayList<Node>();
    for (int index : community) {
      nodeCommunity.add(nodes.get(index));
    }
    resultCommunities.add(nodeCommunity);
  }
  return resultCommunities;
}

// Function to compute modularity
float computeModularity(ArrayList<ArrayList<Integer>> communities, int[][] adjacencyMatrix, int[] degree, int totalEdges) {
  float modularity = 0;
  for (ArrayList<Integer> community : communities) {
    for (int i : community) {
      for (int j : community) {
        modularity += (adjacencyMatrix[i][j] - (degree[i] * degree[j] / (2.0 * totalEdges)));
      }
    }
  }
  return modularity / (2.0 * totalEdges);
}

// Node class
class Node {
  float x, y;
  boolean isInfected;
  int communityId;
  color communityColor;

  Node(float x, float y) {
    this.x = x;
    this.y = y;
    this.isInfected = false;
  }

  void display() {
    noStroke();
    fill(isInfected ? color(255, 0, 0) : communityColor);
    ellipse(x, y, 10, 10);
  }
}

// Edge class
class Edge {
  Node a, b;
  boolean isRed, isGreen;

  Edge(Node a, Node b) {
    this.a = a;
    this.b = b;
    this.isRed = false;
    this.isGreen = false;
  }

void display() {
  strokeWeight(2);

  // Factor to determine the gradient (you can replace this with your infection level variable)
  float infectionLevel = 0.5;  // Example: you could use a variable to adjust this dynamically
  
  if (isRed) {
    // Calculate the distance from point 'a' to 'b'
    float distance = dist(a.x, a.y, b.x, b.y);

    // Loop through the line and set the color based on the position
    for (float t = 0; t <= 1; t += 0.01) {
      // Calculate the current position on the line using interpolation
      float xPos = lerp(a.x, b.x, t);
      float yPos = lerp(a.y, b.y, t);

      // Calculate the current color as a blend between red and yellow
      color edgeColor = lerpColor(color(255, 0, 0), color(255, 255, 0), t);  // 't' varies from 0 (red) to 1 (yellow)
      stroke(edgeColor);

      // Draw a small point at the current position
      point(xPos, yPos);
    }
  } else if (isGreen) {
    stroke(0, 255, 0, 100);  // Non-infected edges in green
    line(a.x, a.y, b.x, b.y);  // Draw the line in green
  } else {
    stroke(100, 100, 100, 50); // Neutral edges in gray
    line(a.x, a.y, b.x, b.y);  // Draw the line in gray
  }
}
}


