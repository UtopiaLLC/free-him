import json
import tkinter as tk

# 0 = vacent/nothing selected
# 1 = target selected
# 2 = node selected
# 3 = deletion
# 4 = draw line
nothing_selected = 0
target_selected = 1
node_selected = 2
deletion = 3
draw_line = 4

empty_tile = 0
target_tile = 1
node_tile = 2
line_tile = 4

mode = 0
rol_num = 13
col_num = 13
# 0 = empty tile
# 1 = target tile
# 2 = node tile
# 4 = line tile
grid = int[rol_num][col_num]

class Target:
    def __init__(self, contents = None):
        if contents == None:
            self.combos = set()
            self.pos = (0,0)
            self.name = ''
            self.suspicion = 1
            self.max_stress = 100
            self.neighbors = set()
            self.starting_stress = 0
            self.lines = set() # a set with int 2-tuples (x, y)
            # self.factnodes = dict()
        else:
            self.name = contents['name']
            self.combos = {(combo['overwritten'], combo['summary'], int(combo['modified_dam']), combo['nodes'])
                                    for combo in contents['combos']}
            self.pos = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.suspicion = int(contents['suspicion'])
            self.max_stress = int(contents['max_stress'])
            self.starting_stress = int(contents['starting_stress'])
            self.neighbors = set(contents['neighbors'])
            self.lines = set(contents['lines'])
            # self.factnodes

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass

class FactNode:
    def __init__(self, pos = (0,0), contents = None):
        '''
            contents is a dictionary containing factnode data 
        '''
        if contents == None:
            self.name = ''
            self.title = ''
            self.position = pos
            self.children = set()
            # self.parent = None
            # self.connection_to_parent = list()
            self.player_stress_dam = 0
            self.stress_dam = 0
            self.summary = ''
            self.contents = ''
            self.locked = True
        else:
            self.name = contents['name']
            self.title = contents['title']
            self.position = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.children = {FactNode(contents = factnode) for factnode in contents['children']}
            # self.parent =
            # self.connection_to_parent = [(int(con_route[0]), int(con_route[1])) for con_route in contents['connection_to_parent']]
            self.player_stress_dam = int(contents['player_stress_dam'])
            self.stress_dam = contents['stress_dam']
            self.summary = contents['summary']
            self.contents = contents['contents']
            self.locked = bool(contents['locked'])

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass


class View:
    def __init__(self):
        self.controller = None
        self.root = tk.Tk('main')
        self.canvas = tk.Canvas(self.root, height=1000, width=1000, bg='white')
        self.root.title('very serious legit level editor')
        self.canvas.pack(fill=tk.BOTH, expand=True)
        self.canvas.bind('<Configure>', self.draw_everything)
        self.canvas.bind('<Button-1>', self.controller.add_object)
        self.canvas.bind('<Button-2>', self.open_new_menu)
        self.root.mainloop()
        
    def add_controller(self, controller):
        self.controller = controller

    def draw_everything(self, event = None, rows = rol_num, cols = col_num):
        w = self.canvas.winfo_width()
        h = self.canvas.winfo_height()
        self.canvas.delete('grid_line')
        self.canvas.delete('target')
        self.canvas.delete('connection')
        #self.canvas.delete('grid_line')

        for i in range(0, w, int(w/rows)):
            self.canvas.create_line([i, 0], [i, h], tag = 'grid_line')
        
        for i in range(0, h, int(h/cols)):
            self.canvas.create_line([0, i], [w, i], tag = 'grid_line')

        #TODO: add all objects <-- need brian's help for this
        #TODO: add thickened lines to indicate a connection
    """
    def draw_object(self, event, row = 2, col = 2):
        w = self.canvas.winfo_width()
        h = self.canvas.winfo_height()
        row = (event.x - event.x%(w/rol_num))/rol_num
        col = (event.y - event.y%(h/col_num))/col_num
        self.canvas.create_text((row*w)/rol_num - (w/(rol_num*2))),(col*h)/col_num - (h/(col_num*2)), font = "WingDings", text = 'T', tag = 'target')
    """

    def open_new_menu(self, event):
        #TODO: open a different menu depending on the object that was right clicked on. This should be doable since 1view have the model
        pass


    def open_gui(self, active_element):
        if isinstance(active_element, Target):
            # open menu with fields
            pass
        elif isinstance(active_element, FactNode):
            # open menu with fields
            pass
        else:
            pass

    def close_gui(self, active_element):
        if isinstance(active_element, Target):
            # save contents
            pass
        elif isinstance(active_element, FactNode):
            # save contents
            pass
        else:
            pass

# 0 = vacent/nothing selected
# 1 = target selected
# 2 = node selected
# 3 = deletion
# 4 = draw line

# 0 = empty tile
# 1 = target tile
# 2 = node tile
# 4 = line tile
class Controller:
    def __init__(self):
        self.a = ''
        self.level = None
        
    def add_level(self, level):
        self.level = level

    def add_object(self, event):
        w = self.level.view.canvas.winfo_width()
        h = self.level.view.canvas.winfo_height()
        rol = (event.x - event.x%(w/rol_num))/rol_num
        col = (event.y - event.y%(h/col_num))/col_num
        
        if mode == node_selected:
            if grid[rol][col] == empty_tile
                self.level.factnodes.add(FactNode(pos=(rol,col)))
        elif mode == deletion:
            if grid[rol][col] == node_tile:
                for f in self.level.factnodes:
                    if f.pos[0] == rol && f.pos[1] == col:
                        self.level.factnodes.remove(f)
            if grid[rol][col] == line_tile:
                for l in self.level.target.lines:
                    if l.pos[0] == rol && l.pos[1] == col:
                        self.level.target.lines.remove(l)
        elif mode == draw_line:
            if grid[rol][col] == empty_tile:
                self.level.target.lines.add((rol, col))


    def draw_line(self, event):
        x, y = event.x, event.y
        if old_coords:
            x1, y1 = old_coords
            self.level.view.canvas.create_line(x, y, x1, y1)
        old_coords = x, y
    
    def delete_line(self, event):
        #TODO
        pass

class Level:
    def __init__(self, file = None):
        if file == None:
            self.target = None 
            self.factnodes = set()
        else:
            jason = json.load(file)
            self.target = Target(contents = jason)
            self.factnodes = { FactNode(contents = cont) for cont in jason['factnodes'] }
        self.active_element = None 
        self.drawing = False # self.drawing if drawing a FactNode path
        self.view = None

    def add_view(self, view):
        self.view = view


    def element_at(self, pos):
        pos = (int(pos[0]), int(pos[1]))
        if pos == 0:
            return self.target
        else:
            for factnode in self.factnodes:
                if factnode.pos == pos:
                    return factnode
        return None

    def click(self, pos):
        if self.drawing:
            pass
        if self.active_element != None:
            self.active_element.close_gui()
        self.active_element = self.click(pos)
        if self.active_element != None:
            self.active_element.open_gui()


m = Level()
v = View()
c = Controller()

m.add_view(v)
v.add_controller(c)
c.add_level(m)