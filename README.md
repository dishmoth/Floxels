# Floxels
Floxels are cute, but not very smart. They live in a maze, and their hobbies are hanging
around in gangs, and converting other Floxels to their colour. 

![Floxels](http://dishmoth.com/wp-content/uploads/2012/04/pic_fl_large-1.png)

Play on Windows/OSX/Linux:
[itch.io](https://dishmoth.itch.io/floxels)

Play on Android:
[Google Play](https://play.google.com/store/apps/details?id=com.dishmoth.floxels.android),
[Amazon Appstore](https://www.amazon.com/dp/B01MZ2OG9P)

Homepage:
[dishmoth.com](http://dishmoth.com/games/floxels/)

### Setting up
The game is built with [libGDX](https://libgdx.badlogicgames.com/).
Set up your development environment following the
[libGDX instructions](https://github.com/libgdx/libgdx/wiki).
Clone or download the repository, and import it into your development environment to build
and run.

### How it works (the science bit)
Floxels behave according to a mash-up of half-remembered bits of physics, hacked together
with no aim other than to give a nice-looking effect.

The starting point is to fill the maze with an invisible fluid.  The fluid is
[incompressible](https://en.wikipedia.org/wiki/Incompressible_flow), simply amounting to
a two-dimensional velocity field subject to a divergence-free constraint (for the most
part) and boundary conditions at the maze walls.  The boundary conditions might be
expected to say that no fluid flows through the walls, but in fact having a bit of an
in-flow everywhere keeps the Floxels from hugging the walls.  In effect, extra
fluid is being continuously pumped into the maze through the walls, which seems unphysical, 
but doesn't cause any problems mathematically.

The fluid's velocity field is calculated by solving
[Poisson's equation](https://en.wikipedia.org/wiki/Poisson%27s_equation)
for the given boundary conditions.  The solution of the equation is a potential field, and
the gradient of that is the fluid's velocity field.  The equation is solved at every time-step
of the simulation by the [multigrid method](https://en.wikipedia.org/wiki/Multigrid_method)
(with Gauss-Seidel smoothing).  Usually the numerical solver would iterate until the
solution converges, but in this case we apply the scientific principle of "it's just a game,
no one cares" and save a lot of computational effort by only doing one iteration.

The Floxels themselves are particles that float (advect) in the invisible fluid.  They take
on the local velocity of the fluid, and go wherever the flow takes them.  But the Floxels 
also influence the behaviour of the fluid in a way that pulls them (or at least the ones 
with the same colour) together into clumps.

The Floxels are [charged particles](https://en.wikipedia.org/wiki/Electrostatics), 
although not quite following the traditional 'positive' and 'negative' roles.
Each one has an 'attractive' charge that pulls other Floxels (of the same colour) towards
it.  To avoid all of the Floxels pulling each other together into a single tiny clump, the
attractive charge is spread over a small area around each Floxel, with a smaller 'repulsive'
charge at the centre.  This makes the Floxels huddle together while still respecting each
other's personal space.  (This balance between attraction and repulsion is similar to 
how [nuclear forces](https://en.wikipedia.org/wiki/Nuclear_force) hold protons and
neutrons together in the nucleus of an atom.)

The charges from the Floxels create an electric field through the maze.  At this point the
laws of physics all go a bit wonky.  As it happens, electric fields obey Poisson's equation,
which is already being used to generate the fluid's velocity field.  Not wanting to let a
good coincidence go to waste, the Floxels use just one field for both the electrostatics 
and the fluid flow, despite physics claiming that these are completely different things.
Each Floxel adds its attractive and repulsive charges as source terms to Poisson's equation
for the fluid's velocity field (breaking the original constraint that the fluid flow should
have zero divergence).  This seems horribly, horribly wrong, but actually works quite nicely.
In effect, an attractive charge causes fluid to be sucked out of the maze, pulling in 
any nearby Floxels, while a repulsive charge cause fluid to be squirted into the maze,
pushing Floxels away.

That's how things work for a single-coloured population of Floxels.  With two different
colours of Floxel, the maze hosts two different fluids, one for each population.
The fluids don't interact directly, but Floxels of one population appear as charges in
the fluid of the other population.  Usually the two populations of Floxels behave like 
predators and prey, with the prey Floxels adding attractive charges to the predator fluid, 
and the predators adding repulsive charges to the prey fluid, meaning that one population 
runs while the other chases.

When Floxels of different colours get close to each other they fight, but there's nothing
scientific about that.

Finally, despite the Floxel simulation having originated through such a shameless
misuse of scientific concepts, it's interesting to see that more deliberate work on
[crowd modelling](http://grail.cs.washington.edu/projects/crowd-flows/)
actually uses some similar techniques, in particular constructing potential fields for 
different populations within a crowd to determine movement direction.
