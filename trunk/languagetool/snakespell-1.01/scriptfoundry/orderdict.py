class OrderDict:
    def __init__(self,dict={}):
	self.dict ={}
	self.list=[]
	if dict is not None: self.update(dict)
    def __repr__(self): 
	return repr(self.list)
	
    def __cmp__(self, dict):
        if isinstance(dict, OrderDict):
            return cmp(self.dict, dict.dict) and cmp(self.list, self.list)

    def __len__(self): 
	return len(self.list)

    def __getitem__(self, key): 
	if type(key) is type(1):
		return self.dict[self.list[key]]
	else:
		return self.dict[key]

    def __setitem__(self, key, item): 
	if type(key) is type(1):
		self.dict[self.list[key]] = item
	else:
		self.dict[key] = item
		self.list.append(key)

    def __delitem__(self, key): 
	del self.dict[key]
	self.list.remove(key)

    def clear(self): 
	self.dict.clear()
    def copy(self):
        if self.__class__ is OrderDict:
            return OrderDict(self.dict)
        import copy
        return copy.copy(self)

    def keys(self): return self.dict.keys()
    def items(self): return self.dict.items()
    def values(self): return self.dict.values()
    def has_key(self, key): return self.dict.has_key(key)
    def update(self, dict):
        if isinstance(dict, OrderDict):
            self.dict.update(dict.dict)
        elif isinstance(dict, type(self.dict)):
            self.dict.update(dict)
        else:
            for k, v in dict.items():
                self.dict[k] = v
    def get(self, key, failobj=None):
	try:
		return  self.dict[key]
	except:
		return failobj

	
    def __getslice__(self, i, j):
        i = max(i, 0); j = max(j, 0)
        userlist = self.__class__()
        userlist.list[:] = self.list[i:j]
	for key in userlist.list:
		userlist.dict[key] = self.dict[key]
        return userlist

    def __setslice__(self, i, j, other):
        i = max(i, 0); j = max(j, 0)
        if isinstance(other, OrderDict):
            self.list[i:j] = other.list
	for key in self.list[i:j]:
		self.dict[key] = other.dict[key]
	
    def __delslice__(self, i, j):
        i = max(i, 0); j = max(j, 0)
        for key in   self.list[i:j]:
		del self.dict[key]
	del self.list[i:j]

    def __add__(self, other):
        if isinstance(other, OrderDict):
            return self.__class__(self.data + other.data)
        elif isinstance(other, type(self.data)):
            return self.__class__(self.data + other)
        else:
            return self.__class__(self.data + list(other))
    def __radd__(self, other):
        if isinstance(other, UserList):
            return self.__class__(other.data + self.data)
        elif isinstance(other, type(self.data)):
            return self.__class__(other + self.data)
        else:
            return self.__class__(list(other) + self.data)
    def __mul__(self, n):
        return self.__class__(self.data*n)
    __rmul__ = __mul__
    def append(self, key, item): self.list.append(key); self.dict[key] = item
    def insert(self, i, key, item): self.list.insert(i, key); self.dict[key] = item
    def pop(self, i=-1): 
	key =  self.list.pop(i);  
	item =  self.dict[key]
	del self.dict[key]
	return key, item

    def remove(self, item): 
	key = self.list[item]
	self.list.remove(item)
	del self.dict[key]
    def count(self, item): return self.list.count(item)
    def index(self, item): return self.list.index(item)
    def reverse(self): self.list.reverse()
    def sort(self, *args): apply(self.list.sort, args)
    #def extend(self, other):
    #    if isinstance(other, UserList):
    #        self.data.extend(other.data)
    #    else:
    #        self.data.extend(other)

if __name__ =='__main__':
	md = OrderDict()
	md['one'] = 1
	md['two'] = 2
	md['three'] = 3
	md['one'] = 3
	md['four'] = 4
	print md.__dict__
	print md['one']
	print md[0:-1]
	for i in md:
		print i

